package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReleaseEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(ReleaseEventRepo.class.getName());

    public ReleaseEventRepo() {
        super();
        collection = database.getCollection("releaseEvent");
    }
}
