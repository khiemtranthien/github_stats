package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AllEventsRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(AllEventsRepo.class.getName());

    public AllEventsRepo() {
        super();
        collection = database.getCollection("githubEvents");
    }
}
