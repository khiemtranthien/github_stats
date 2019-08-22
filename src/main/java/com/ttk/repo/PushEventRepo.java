package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PushEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(PushEventRepo.class.getName());

    public PushEventRepo() {
        super();
        collection = database.getCollection("pushEvent");
    }
}
