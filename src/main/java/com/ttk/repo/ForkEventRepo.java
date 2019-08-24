package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForkEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(ForkEventRepo.class.getName());

    public ForkEventRepo() {
        super();
        collection = database.getCollection("forkEvent");
    }

    public List<Document> countForkByRepoIds(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        params.put("repoIds:arrayNum", repoIds);

        List<Document> pipeline = parseQueryString("fork.js", "TotalForkByRepoId", params);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);
        return result;
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        List result = new ForkEventRepo().countForkByRepoIds(Arrays.asList(119611077, 78753723, 22956623));
        LOGGER.info(result);
    }
}
