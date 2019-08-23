package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PullRequestEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(PullRequestEventRepo.class.getName());

    public PullRequestEventRepo() {
        super();
        collection = database.getCollection("pullRequestEvent");
    }
}
