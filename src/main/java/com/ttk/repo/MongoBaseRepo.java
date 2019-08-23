package com.ttk.repo;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import com.ttk.utils.AppProperties;
import com.ttk.utils.JavascriptUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(MongoBaseRepo.class.getName());

    MongoClient mongoClient;
    MongoDatabase database;

    MongoCollection<Document> collection;

    public MongoBaseRepo() {
        AppProperties appConfig = AppProperties.getInstance();
        String mongoHost = appConfig.get("mongo.host");
        int mongoPort = Integer.valueOf(appConfig.get("mongo.port"));
        String db = appConfig.get("mongo.db");

        mongoClient = new MongoClient(mongoHost, mongoPort);
        database = mongoClient.getDatabase(db);
    }

    public FindIterable getIterable(Document where) {
        return getIterable(where, null, null);
    }

    public FindIterable getIterable(Document where, Document projection) {
        return getIterable(where, projection, null);
    }

    public FindIterable getIterable(Document where, Document projection, Document sort) {
        FindIterable query;

        if(where != null) {
            query = collection.find(where);
        } else {
            query = collection.find();
        }

        if(projection != null) {
            query = query.projection(projection);
        }

        if(sort != null) {
            query = query.sort(sort);
        }

        return query;
    }

    public List get(Document where) {
        return get(where, null, null);
    }

    public List get(Document where, Document projection) {
        return get(where, projection, null);
    }

    public List get(Document where, Document projection, Document sort) {
        FindIterable query;

        if(where != null) {
            query = collection.find(where);
        } else {
            query = collection.find();
        }

        if(projection != null) {
            query = query.projection(projection);
        }

        if(sort != null) {
            query = query.sort(sort);
        }

        MongoCursor cursor = query.iterator();

        return getResponse(cursor);
    }

    public void upsert(Document doc) throws IllegalArgumentException {
        if (!doc.containsKey("id")) {
            throw new IllegalArgumentException("ID is not available in document");
        }

        Document query = new Document("_id", doc.get("id"));
        Document update = new Document("$set", doc);

        collection.updateOne(query, update, (new UpdateOptions()).upsert(true));
    }

    public List<Document> aggregation(List<Document> pipeline) {
        AggregateIterable<Document> output = collection.aggregate(pipeline).allowDiskUse(true).maxTime(10, TimeUnit.MINUTES);

        MongoCursor<Document> cursor = output.iterator();

        return getResponse(cursor);
    }

    public List<Document> parseQueryString(String queryFile, String queryName) throws ScriptException, NoSuchMethodException {
        List<Document> pipline = new ArrayList<>();

        String jsonStr = JavascriptUtil.getQueryString(queryFile, queryName);
        BasicDBList jsonArray = (BasicDBList)JSON.parse(jsonStr);
        jsonArray.stream().map(obj -> ((DBObject) obj).toMap()).forEach((obj) -> {
            Document doc = new Document(obj);
            pipline.add(doc);
        });

        return pipline;
    }

    public List<Document> getResponse(MongoCursor cursor) {
        List<Document> result = new ArrayList<>();
        while (cursor.hasNext()) {
            Document doc = (Document)cursor.next();
            result.add(doc);
        }
        return result;
    }
}
