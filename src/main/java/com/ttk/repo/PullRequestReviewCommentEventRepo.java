package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PullRequestReviewCommentEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(PullRequestReviewCommentEventRepo.class.getName());

    public PullRequestReviewCommentEventRepo() {
        super();
        collection = database.getCollection("pullRequestReviewCommentEvent");
    }
}
