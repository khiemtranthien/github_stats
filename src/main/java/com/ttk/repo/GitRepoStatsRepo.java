package com.ttk.repo;

import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.List;

public class GitRepoStatsRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoStatsRepo.class.getName());

    public GitRepoStatsRepo() {
        super();
        collection = database.getCollection("gitRepoStats");
    }

    public Integer getMaxPushCount() throws ScriptException, NoSuchMethodException {

        List<Document> pipeline = parseQueryString("repoStats.js", "GetMaxPush", null);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);

        Document row = result.get(0);
        Integer max = (Integer)row.get("max");

        return max;
    }

    public Integer getMaxReleaseCount() throws ScriptException, NoSuchMethodException {
        List<Document> pipeline = parseQueryString("repoStats.js", "GetMaxRelease", null);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);

        Document row = result.get(0);
        Integer max = (Integer)row.get("max");

        return max;
    }

    public Integer getMaxContributorCount() throws ScriptException, NoSuchMethodException {

        List<Document> pipeline = parseQueryString("repoStats.js", "GetMaxContributor", null);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);

        Document row = result.get(0);
        Integer max = (Integer)row.get("max");

        return max;
    }

    public List<Document> getTopHealthiestRepo(Integer size) {
        FindIterable<Document> queryIterator = collection.find().sort(new Document("health_score", -1)).limit(size);
        return getResponse(queryIterator.iterator());
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
        Integer result = new GitRepoStatsRepo().getMaxContributorCount();
        LOGGER.info(result);
    }

}
