package com.ttk.datasouce;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import com.ttk.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Ingestor {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    public void run() {
        try {
            MongoClient mongoClient = new MongoClient("localhost", 27017);
            MongoDatabase database = mongoClient.getDatabase("GHArchive");
            MongoCollection<Document> collection = database.getCollection("githubEvents");

            List<String> files = Datasouce.getDesiredJsonFiles();

            LOGGER.info("Ingesting json files...");
            for (String fileName : files) {
                LOGGER.info(String.format("Ingest file: %s", fileName));
                ingest(collection, fileName);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public void ingest(MongoCollection collection, String fileName) {
        Path myFile = Paths.get(fileName);

        try (Stream<String> stream = Files.lines(myFile)) {

            stream.map(str -> (DBObject) JSON.parse(str)).forEach((dbObject) -> {

                Document eventData = new Document(dbObject.toMap());
                Instant created_ts = Instant.parse((String)eventData.get("created_at"));
                eventData.put("created_at", Date.from(created_ts));
                eventData.put("_id", eventData.get("id"));

                Document query = new Document();
                query.put("_id", eventData.get("id"));

                Document update = new Document();
                update.put("$set", eventData);

                collection.updateOne(query, update, (new UpdateOptions()).upsert(true));
            });

        } catch (IOException e) {
            LOGGER.error(e);
        }
    }
}
