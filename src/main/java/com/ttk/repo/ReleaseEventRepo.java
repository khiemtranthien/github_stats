package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(ReleaseEventRepo.class.getName());

    public ReleaseEventRepo() {
        super();
        collection = database.getCollection("releaseEvent");
    }

    public List<Document> countReleaseByRepoIds(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        params.put("repoIds:arrayNum", repoIds);

        List<Document> pipeline = parseQueryString("release.js", "TotalReleaseByRepoId", params);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);
        return result;
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        List result = new ReleaseEventRepo().countReleaseByRepoIds(Arrays.asList(11581991, 42778193));
        LOGGER.info(result);
    }
}
