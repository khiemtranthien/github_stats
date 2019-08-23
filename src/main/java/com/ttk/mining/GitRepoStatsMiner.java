package com.ttk.mining;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.ttk.datasouce.GitRepoIngestor;
import com.ttk.repo.GitRepoRepo;
import com.ttk.repo.MongoBaseRepo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.List;

public class GitRepoStatsMiner {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoStatsMiner.class.getName());

    public static void main(String[] args) {
        new GitRepoStatsMiner().run();
    }

    public void run() {
        GitRepoRepo gitRepoRepo = new GitRepoRepo();

        Document where = null;  // set where = null to get all
        FindIterable allRepos = gitRepoRepo.getIterable(where);

        FindIterable cursor = allRepos.limit(50);

        while(cursor.iterator().hasNext()) {
            List<Document> items = gitRepoRepo.getResponse(cursor.iterator());

            getRepoStats(items);

            cursor = allRepos.skip(50).limit(50);
        }
    }

    public void getRepoStats(List<Document> items) {
        items.stream().forEach(LOGGER::info);
    }
}
