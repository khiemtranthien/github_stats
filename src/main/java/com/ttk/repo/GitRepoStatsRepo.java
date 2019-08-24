package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GitRepoStatsRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoStatsRepo.class.getName());

    public GitRepoStatsRepo() {
        super();
        collection = database.getCollection("gitRepoStats");
    }

    public Integer getMaxPushCount() {
        return 100;
    }

    public Integer getMaxReleaseCount() {
        return 100;
    }

    public Integer getMaxContributorCount() {
        return 100;
    }
}
