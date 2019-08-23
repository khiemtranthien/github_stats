package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class PushEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(PushEventRepo.class.getName());

    public PushEventRepo() {
        super();
        collection = database.getCollection("pushEvent");
    }

    public void getAveragePushDaily()  throws ScriptException, NoSuchMethodException {
        List<Document> pipeline = parseQueryString("push.js", "RepoPushDaily");

        List<Document> result = aggregation(pipeline);
    }

    public List getPushEvents(String dateFromStr, String dateToStr) {
        Date dateFrom = Date.from(Instant.parse(dateFromStr));
        Date dateTo = Date.from(Instant.parse(dateToStr));

        Document where = new Document("created_at", new Document("$gte", dateFrom))
                .append("created_at", new Document("$lt", dateTo));

        Document projection = new Document("repo", 1);
        List result = get(where, projection);

        return result;
    }

    public static void main(String[] args) {
        List result = new PushEventRepo().getPushEvents("2019-08-19T00:00:00Z", "2019-08-19T01:00:00Z");
        LOGGER.info(result);
    }
}
