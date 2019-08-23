package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IssuesEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(IssuesEventRepo.class.getName());

    public IssuesEventRepo() {
        super();
        collection = database.getCollection("issuesEvent");
    }
}
