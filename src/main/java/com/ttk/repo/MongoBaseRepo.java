package com.ttk.repo;

import com.mongodb.BasicDBList;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.util.JSON;
import com.ttk.utils.AppProperties;
import com.ttk.utils.JavascriptUtil;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    /**
     * Return the Monggo FindIterable which is helpful to do paging large result
     * @param where - Document object
     * @return FindIterable
     */
    public FindIterable iterableGet(Document where) {
        return iterableGet(where, null, null);
    }

    /**
     * Return the Monggo FindIterable which is helpful to do paging large result
     * @param where - Document object
     * @return FindIterable
     */
    public FindIterable iterableGet(Document where, Document projection) {
        return iterableGet(where, projection, null);
    }

    /**
     * Return the Monggo FindIterable which is helpful to do paging large result
     * @param where - Document object
     * @return FindIterable
     */
    public FindIterable iterableGet(Document where, Document projection, Document sort) {
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

    public List<Document> get(Document where) {
        return get(where, null, null);
    }

    public List<Document> get(Document where, Document projection) {
        return get(where, projection, null);
    }

    public List<Document> get(Document where, Document projection, Document sort) {
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

    /**
     * Monggo aggregation query is stored in project's resources folder group by event type file name.
     * The query may have some parameters placeholder, if any, this function will populate it.
     * Output is a full JSON query string
     * @param queryFile - Event type file in resources folder
     * @param queryName - One event type file has several queries. Specify the query name you want to get
     * @param params - A map parameters for query string. Look at query string to know the parameter name
     * @return JSON query string
     * @throws ScriptException
     * @throws NoSuchMethodException
     */
    public List<Document> parseQueryString(String queryFile, String queryName, Map<String, Object> params) throws ScriptException, NoSuchMethodException {
        List<Document> pipline = new ArrayList<>();

        String jsonStr = JavascriptUtil.getQueryString(queryFile, queryName);
        if(params != null) {
            jsonStr = populateParams(jsonStr, params);
        }

        BasicDBList jsonArray = (BasicDBList)JSON.parse(jsonStr);
        jsonArray.stream().map(obj -> ((DBObject) obj).toMap()).forEach((obj) -> {
            Document doc = new Document(obj);
            pipline.add(doc);
        });

        return pipline;
    }

    public String populateParams(String jsonQuery, Map<String, Object> params) {
        Map<String, String> newParams = new HashMap<>();

        params.forEach((k, v) -> {
            String[] pairs = k.split(":");

            String newVal;

            if(pairs.length > 1) {
                String paramType = pairs[1];

                if (paramType.equals("str")) {
                    newVal = String.format("\"%s\"", v);

                } else if (paramType.equals("num")) {
                    newVal = String.valueOf(v);

                } else if (paramType.equals("arrayStr")) {
                    List castListV = (List)v;
                    newVal = castListV.stream().map(str -> String.format("\"%s\"", str)).collect(Collectors.joining(",")).toString();

                } else if (paramType.equals("arrayNum")) {
                    List castListV = (List)v;
                    newVal = castListV.stream().map(String::valueOf).collect(Collectors.joining(",")).toString();

                } else {
                    throw new IllegalStateException("param type invalid");
                }
            } else {
                if (v instanceof String) {
                    newVal = String.format("\"%s\"", v);

                } else if(v instanceof Number) {
                    newVal = String.valueOf(v);

                } else {
                    throw new IllegalStateException("value type invalid");
                }
            }

            newParams.put(pairs[0], newVal);
        });

        StringSubstitutor sub = new StringSubstitutor(newParams, "\"%(", ")\"");

        String fmtQuery = sub.replace(jsonQuery);
        return fmtQuery;
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
