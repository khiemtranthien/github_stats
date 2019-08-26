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

    public static void main(String[] args) {
        try {
            new RepoHealthMiner().run();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void run() throws InterruptedException, ScriptException, NoSuchMethodException {
         /* For each repo, health score formula:
            health_score =
                push_count / max_push *
                release_count / max_release *
                contributor_count / max_contributor *
                issue_open_time / min_open_time
         */

        Integer maxPush = gitRepoStatsRepo.getMaxPushCount();
        Integer maxRelease = gitRepoStatsRepo.getMaxReleaseCount();
        Integer maxContributor = gitRepoStatsRepo.getMaxContributorCount();
        Double minIssueOpenTime = gitRepoStatsRepo.getMinIssueOpenTiem();

        Map<String, Number> baseParamsMap = new HashMap<>();
        baseParamsMap.put("maxPush", maxPush);
        baseParamsMap.put("maxRelease", maxRelease);
        baseParamsMap.put("maxContributor", maxContributor);
        baseParamsMap.put("minIssueOpenTime", minIssueOpenTime);

        LOGGER.info(String.format("Max Push: %d, " +
                "Max Release: %d, " +
                "Max Contributor: %d, " +
                "Min issue open time: %.2f", maxPush, maxRelease, maxContributor, minIssueOpenTime));

        Document where = null;  // set where = null to get all rows
        FindIterable allRepos = gitRepoStatsRepo.iterableGet(where);

        int page = 1;
        int pageSize = 500;

        FindIterable cursor = allRepos.limit(pageSize);

        while(cursor.iterator().hasNext()) {
            List<Document> items = gitRepoStatsRepo.getResponse(cursor.iterator());

            calculateHealthScore(items, baseParamsMap);

            cursor = allRepos.skip(pageSize * page).limit(pageSize);

            LOGGER.info(String.format("Calculate counter: %d", page * pageSize));
            page += 1;
        }

        // shut down Executor after complete mining
        shutdownExecutor();
    }

    private void calculateHealthScore(List<Document> repos, Map<String, Number> baseParamsMap) throws InterruptedException {
        List<List<Document>> partitions = ListUtils.partition(repos, 100);

        List<Callable<String>> callables = new ArrayList<>();
        for (List<Document> partition : partitions) {
            callables.add(callable(partition, baseParamsMap));
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

    private Callable<String> callable(List<Document> repoPartition, Map<String, Number> baseParamsMap) {
        return () -> {
            LOGGER.info(String.format("Calculate partition %d repo(s)", repoPartition.size()));

            Integer maxPush = (Integer) baseParamsMap.get("maxPush");
            Integer maxRelease = (Integer) baseParamsMap.get("maxRelease");
            Integer maxContributor = (Integer) baseParamsMap.get("maxContributor");
            Double minIssueOpenTime = (Double) baseParamsMap.get("minIssueOpenTime");

            try {
                repoPartition.forEach(doc -> {
                    Integer repoId = (Integer)doc.get("id");
                    LOGGER.debug(repoId);

                    Integer push = (Integer)doc.get("push");
                    Integer release = (Integer)doc.get("release");
                    Integer contributor = (Integer)doc.get("contributor");
                    Double issueOpened = (Double)doc.get("issue_opened_avg");

                    Double healthScore =
                            push/(double)maxPush *
                            release/(double)maxRelease *
                            contributor/(double)maxContributor *
                            issueOpened / minIssueOpenTime;

                    Document updatedStats = new Document("id", repoId);
                    updatedStats.append("health_score", healthScore);
                    updatedStats.append("health_mining_at", new Date());

                    gitRepoStatsRepo.upsert(updatedStats);
                });

            } catch (Exception e) {
                LOGGER.error(e);
                e.printStackTrace();
                return String.format("Calculate failed. Error: %s", e.getMessage());
            }
            return String.format("Calculate %d repo(s) successfully", repoPartition.size());
        };
    }
}
