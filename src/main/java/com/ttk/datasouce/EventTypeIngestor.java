package com.ttk.datasouce;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.ttk.Main;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class EventTypeIngestor {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    MongoBaseRepo repo;

    public static void main(String[] args) {
        EventTypeIngestor app = new EventTypeIngestor();
        List<String> ingestEvents = Arrays.asList(
                Constants.RELEASE_EVENT,
                Constants.PUSH_EVENT,
                Constants.FORK_EVENT,
                Constants.ISSUES_EVENT,
                Constants.PULL_REQUEST_EVENT,
                Constants.PULL_REQUEST_REVIEW_COMMENT_EVENT
        );

        for(String eventType: ingestEvents) {
            String dateFrom = "2019-08-18";
            String dateTo = "2019-08-20";
            app.run(eventType, dateFrom, dateTo);
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

            } catch (IOException e) {
                LOGGER.error(e);
                return String.format("Insert file %s failed. Error: %s", jsonFileName, e.getMessage());
            }
            return String.format("Insert file %s successfully", jsonFileName);
        };
    }

    public void run(String eventType, String dateFrom, String dateTo) {
        if(!Constants.EVENT_TYPES.contains(eventType)) {
            throw new IllegalArgumentException(String.format("Event type: %s not found in check list", eventType));
        }

        LOGGER.info(String.format("Ingest for event: %s", eventType));

        LOGGER.info("Init repo");
        repo = RepoFactory.getRepo(eventType);

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
                    Document eventData = new Document(dbObject.toMap());
                    Instant created_ts = Instant.parse((String) eventData.get("created_at"));
                    eventData.put("created_at", Date.from(created_ts));

                    repo.upsert(eventData);
                }
            });
        }
    }
}
