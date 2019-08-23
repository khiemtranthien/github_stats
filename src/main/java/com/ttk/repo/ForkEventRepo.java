package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ForkEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(ForkEventRepo.class.getName());

    public ForkEventRepo() {
        super();
        collection = database.getCollection("forkEvent");
    }
}
