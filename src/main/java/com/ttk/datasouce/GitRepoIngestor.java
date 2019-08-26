package com.ttk.datasouce;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.ttk.Main;
import com.ttk.repo.GitRepoRepo;
import com.ttk.repo.MongoBaseRepo;
import com.ttk.repo.RepoFactory;
import com.ttk.utils.AppProperties;
import com.ttk.utils.Constants;
import com.ttk.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class GitRepoIngestor {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    MongoBaseRepo repo;

    public GitRepoIngestor() {
        repo = new GitRepoRepo();
    }

    public static void main(String[] args) {
        try {
            AppProperties appConfig = AppProperties.getInstance();

            String dateFrom = appConfig.get("data.dateFrom");  // 2019-08-16
            String dateTo = appConfig.get("data.dateTo");  // 2019-08-17

            new GitRepoIngestor().run(dateFrom, dateTo);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    private Callable<String> callable(String jsonFileName, String eventType) {
        return () -> {
            LOGGER.info(String.format("Ingest file: %s", jsonFileName));
            try {
                // Step 1: Extract gz file to json folder
                LOGGER.debug("Unzip Gz file");
                AppProperties appConfig = AppProperties.getInstance();
                String zipFolder = appConfig.get("data.zipFolder");
                String jsonFolder = appConfig.get("data.jsonFolder");
                String gzFilePath = zipFolder + jsonFileName + ".gz";
                String outputFilePath = jsonFolder + jsonFileName;

                FileUtil.unzipGzFiles(gzFilePath, outputFilePath);

                // Step 2: Ingest
                LOGGER.debug("Ingest");
                ingest(outputFilePath, eventType);

                // Step 3: Remove json file (a must to free disk storage)
                LOGGER.debug("Remove json file");
                boolean isSuccess = FileUtil.removeFile(outputFilePath);
                if(!isSuccess) {
                    throw new FileNotFoundException(String.format("%s: File not found or not a valid file", outputFilePath));
                }

            } catch (Exception e) {
                LOGGER.error(e);
                return String.format("Insert file %s failed. Error: %s", jsonFileName, e.getMessage());
            }
            return String.format("Insert file %s successfully", jsonFileName);
        };
    }

    public void run(String dateFrom, String dateTo) {
        String eventType = Constants.PUSH_EVENT;

        if(!Constants.EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException(String.format("Event type: %s not found in check list", eventType));
        }

        LOGGER.info(String.format("Ingest Git Repo from %s to %s", dateFrom, dateTo));

        try {
            List<String> requiredFileNames = DatasouceHelper.generateJsonFileNamesForDateRange(dateFrom, dateTo);

            ExecutorService executor = Executors.newFixedThreadPool(5);

            LOGGER.info("Ingesting json files...");

            List<Callable<String>> callables = new ArrayList<>();
            for (String fileName : requiredFileNames) {
                callables.add(callable(fileName, eventType));
            }

            executor.invokeAll(callables)
                    .stream()
                    .map(future -> {
                        try {
                            return future.get();
                        }
                        catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .forEach(LOGGER::info);

            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.MINUTES)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

        } catch (Exception e) {
            LOGGER.error(e);
        }

        LOGGER.info("Done.");
    }

    private void ingest(String filePath, String eventType) throws IOException {
        Path myFile = Paths.get(filePath);

        try (Stream<String> stream = Files.lines(myFile)) {

            stream.map(str -> (DBObject) JSON.parse(str)).forEach((dbObject) -> {
                if (dbObject.get("type").equals(eventType)) {
                    BasicDBObject repoObj = (BasicDBObject) dbObject.get("repo");

                    Document repoDoc = new Document(repoObj.toMap());

                    if(dbObject.containsField("org")) {
                        BasicDBObject orgObj = (BasicDBObject) dbObject.get("org");
                        repoDoc.append("org", new Document(orgObj.toMap()));
                    }
                    repo.upsert(repoDoc);
                }
            });
        }
    }
}
