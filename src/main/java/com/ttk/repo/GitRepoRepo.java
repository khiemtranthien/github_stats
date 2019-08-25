package com.ttk.repo;

import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.Arrays;
import java.util.List;

public class GitRepoRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoRepo.class.getName());

    public GitRepoRepo() {
        super();
        collection = database.getCollection("gitRepo");
    }

    public List<Document> getRepoInfoByIds(List<Integer> repoIds) {
        FindIterable<Document> queryIterator = collection.find(new Document("_id", new Document("$in", repoIds)));

        return getResponse(queryIterator.iterator());
    }

    public static void main(String[] args) {
        List<Document> result = new GitRepoRepo().getRepoInfoByIds(Arrays.asList(3618133, 5973855, 34526884));
        LOGGER.info(result);
    }
}
