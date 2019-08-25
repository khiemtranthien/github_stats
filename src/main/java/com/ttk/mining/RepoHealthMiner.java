package com.ttk.mining;

import com.mongodb.client.FindIterable;
import com.ttk.repo.GitRepoStatsRepo;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.*;
import java.util.concurrent.Callable;

public class RepoHealthMiner extends BaseMiner {
    private static final Logger LOGGER = LogManager.getLogger(RepoHealthMiner.class.getName());

    GitRepoStatsRepo gitRepoStatsRepo;

    public RepoHealthMiner() {
        super();
        gitRepoStatsRepo = new GitRepoStatsRepo();
    }

    public static void main(String[] args) throws InterruptedException, ScriptException, NoSuchMethodException {
        new RepoHealthMiner().run();
    }

    public void run() throws InterruptedException, ScriptException, NoSuchMethodException {
         /* For each repo, health score formula:
        health_score = push_count/max_push * release_count/max_release * contributor_count/max_contributor
         */

        Integer maxPush = gitRepoStatsRepo.getMaxPushCount();
        Integer maxReleaseCount = gitRepoStatsRepo.getMaxReleaseCount();
        Integer maxContributorCount = gitRepoStatsRepo.getMaxContributorCount();

        Map<String, Integer> maxParamsMap = new HashMap<>();
        maxParamsMap.put("maxPush", maxPush);
        maxParamsMap.put("maxReleaseCount", maxReleaseCount);
        maxParamsMap.put("maxContributorCount", maxContributorCount);

        Document where = null;  // set where = null to get all rows
        FindIterable allRepos = gitRepoStatsRepo.iterableGet(where);

        int page = 1;
        int pageSize = 500;

        FindIterable cursor = allRepos.limit(pageSize);

        while(cursor.iterator().hasNext()) {
            List<Document> items = gitRepoStatsRepo.getResponse(cursor.iterator());

            calculateHealthScore(items, maxParamsMap);

            cursor = allRepos.skip(pageSize * page).limit(pageSize);

            LOGGER.info(String.format("Calculate counter: %d", page * pageSize));
            page += 1;
        }

        // shut down Executor after complete mining
        shutdownExecutor();
    }

    public void calculateHealthScore(List<Document> repos, Map<String, Integer> maxParamsMap) throws InterruptedException {
        List<List<Document>> partitions = ListUtils.partition(repos, 100);

        List<Callable<String>> callables = new ArrayList<>();
        for (List<Document> partition : partitions) {
            callables.add(callable(partition, maxParamsMap));
        }

        executor.invokeAll(callables)
                .stream()
                .map(future -> {
                    try {
                        return future.get();
                    }
                    catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                })
                .forEach(LOGGER::info);
    }

    private Callable<String> callable(List<Document> repoPartition, Map<String, Integer> maxParamsMap) {
        return () -> {
            LOGGER.info(String.format("Calculate partition %d repo(s)", repoPartition.size()));

            Integer maxPush = maxParamsMap.get("maxPush");
            Integer maxRelease = maxParamsMap.get("maxReleaseCount");
            Integer maxContributor = maxParamsMap.get("maxContributorCount");

            LOGGER.info(String.format("Max Push: %d, Max Release: %d, Max Contributor: %d", maxPush, maxRelease, maxContributor));

            try {
                repoPartition.forEach(doc -> {
                    Integer push = (Integer)doc.get("push");
                    Integer release = (Integer)doc.get("release");
                    Integer contributor = (Integer)doc.get("contributor");

                    Double healthScore = push/(double)maxPush * release/(double)maxRelease * contributor/(double)maxContributor;

                    Integer repoId = (Integer)doc.get("id");

                    Document updatedStats = new Document("id", repoId);
                    updatedStats.append("health_score", healthScore);
                    updatedStats.append("health_mining_at", new Date());

                    gitRepoStatsRepo.upsert(updatedStats);
                });

            } catch (Exception e) {
                LOGGER.error(e);
                return String.format("Calculate failed. Error: %s", e.getMessage());
            }
            return String.format("Calculate %d repo(s) successfully", repoPartition.size());
        };
    }
}
