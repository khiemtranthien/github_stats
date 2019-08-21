package com.ttk.repo;

import com.mongodb.AggregationOptions;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import com.ttk.utils.AppProperties;
import com.ttk.utils.JavascriptReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MongoDBRepo {
    private static final Logger LOGGER = LogManager.getLogger(MongoDBRepo.class.getName());

    MongoClient mongoClient;
    MongoDatabase database;
    MongoCollection<Document> collection;

    public MongoDBRepo() {
        AppProperties appConfig = AppProperties.getInstance();
        String mongoHost = appConfig.get("mongo.host");
        int mongoPort = Integer.valueOf(appConfig.get("mongo.port"));

        mongoClient = new MongoClient(mongoHost, mongoPort);
        database = mongoClient.getDatabase("GHArchive");
        collection = database.getCollection("githubEvents");
    }

    public void upsert(Document eventData) throws IllegalArgumentException {
        if (!eventData.containsKey("id")) {
            throw new IllegalArgumentException("ID is not available in document");
        }
        eventData.put("_id", eventData.get("id"));

        Document query = new Document();
        query.put("_id", eventData.get("id"));

        Document update = new Document();
        update.put("$set", eventData);

        collection.updateOne(query, update, (new UpdateOptions()).upsert(true));
    }

    public void aggregation() throws ScriptException, NoSuchMethodException {
        List<Document> pipeline = parseJsonString();
        AggregateIterable<Document> output = collection.aggregate(pipeline).allowDiskUse(true).maxTime(10, TimeUnit.MINUTES);
        MongoCursor<Document> cursor = output.iterator();

        while (cursor.hasNext()) {
            Document doc = cursor.next();
            LOGGER.info(doc);
        }
    }

    public List<Document> parseJsonString() throws ScriptException, NoSuchMethodException {
        List<Document> pipline = new ArrayList<>();

        String jsonStr = JavascriptReader.getQueryString("push.js");
        BasicDBList jsonArray = (BasicDBList)JSON.parse(jsonStr);
        jsonArray.stream().map(obj -> ((DBObject) obj).toMap()).forEach((obj) -> {
            Document doc = new Document(obj);
            LOGGER.info(doc);
            pipline.add(doc);
        });

        return pipline;
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        new MongoDBRepo().aggregation();
    }
}
