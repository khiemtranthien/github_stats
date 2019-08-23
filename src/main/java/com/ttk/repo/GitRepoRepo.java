package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.List;

public class GitRepoRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoRepo.class.getName());

    public GitRepoRepo() {
        super();
        collection = database.getCollection("gitRepo");
    }


}
