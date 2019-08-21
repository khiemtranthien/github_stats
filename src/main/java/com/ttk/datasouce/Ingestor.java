package com.ttk.datasouce;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import com.ttk.Main;
import com.ttk.repo.MongoDBRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

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

public class Ingestor {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    MongoDBRepo repo;

    public Ingestor() {
        repo = new MongoDBRepo();
    }

    private Callable<String> callable(String fileName) {
        return () -> {
            LOGGER.info(String.format("Ingest file: %s", fileName));
            try {
                ingest(fileName);
            } catch (IOException e) {
                LOGGER.error(e);
                return String.format("Insert file %s failed. Error: %s", fileName, e.getMessage());
            }
            return String.format("Insert file %s successfully", fileName);
        };
    }

    public void run() {
        try {
            List<String> files = Datasouce.getDesiredJsonFilesForIngestion();

            ExecutorService executor = Executors.newFixedThreadPool(5);

            LOGGER.info("Ingesting json files...");

            List<Callable<String>> callables = new ArrayList<>();
            for (String fileName : files) {
                callables.add(callable(fileName));
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
                    .forEach(System.out::println);

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
    }

    public void ingest(String fileName) throws IOException {
        Path myFile = Paths.get(fileName);

        try (Stream<String> stream = Files.lines(myFile)) {

            stream.map(str -> (DBObject) JSON.parse(str)).forEach((dbObject) -> {

                Document eventData = new Document(dbObject.toMap());
                Instant created_ts = Instant.parse((String)eventData.get("created_at"));
                eventData.put("created_at", Date.from(created_ts));

                repo.upsert(eventData);
            });
        }
    }
}
