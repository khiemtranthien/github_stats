package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(PushEventRepo.class.getName());

    public PushEventRepo() {
        super();
        collection = database.getCollection("pushEvent");
    }

    public List<Document> countPushByRepoIds(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        params.put("repoIds:arrayNum", repoIds);

        List<Document> pipeline = parseQueryString("push.js", "TotalPushByRepoId", params);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);
        return result;
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        List result = new PushEventRepo().countPushByRepoIds(Arrays.asList(146959896, 121027550));
        LOGGER.info(result);
    }
}
