package com.ttk.mining;

import com.mongodb.client.FindIterable;
import com.ttk.repo.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class GitRepoStatsMiner extends BaseMiner {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoStatsMiner.class.getName());

    GitRepoStatsRepo gitRepoStatsRepo;

    GitRepoRepo gitRepoRepo;
    PushEventRepo pushEventRepo;
    ReleaseEventRepo releaseEventRepo;
    ForkEventRepo forkEventRepo;

    public GitRepoStatsMiner() {
        super();

        gitRepoStatsRepo = new GitRepoStatsRepo();

        pushEventRepo = new PushEventRepo();
        releaseEventRepo = new ReleaseEventRepo();
        forkEventRepo = new ForkEventRepo();
        gitRepoRepo = new GitRepoRepo();
    }

    public static void main(String[] args) throws InterruptedException {
        new GitRepoStatsMiner().run();
    }

    public void run() throws InterruptedException {
        Document where = null;  // set where = null to get all rows
        FindIterable allRepos = gitRepoRepo.iterableGet(where);

        int page = 1;
        int pageSize = 500;

        FindIterable cursor = allRepos.limit(pageSize);

        while(cursor.iterator().hasNext()) {
            List<Document> items = gitRepoRepo.getResponse(cursor.iterator());

            getRepoStats(items);

            cursor = allRepos.skip(pageSize * page).limit(pageSize);

            LOGGER.info(String.format("Mining counter: %d", page * pageSize));
            page += 1;
        }

        // shut down Executor after complete mining
        shutdownExecutor();
    }

    public void getRepoStats(List<Document> repos) throws InterruptedException {
        // split repos for each thread in executor
        List<List<Document>> partitions = ListUtils.partition(repos, 100);

        List<Callable<String>> callables = new ArrayList<>();
        for (List<Document> partition : partitions) {
            callables.add(callable(partition));
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

    private Callable<String> callable(List<Document> repoPartition) {
        return () -> {
            LOGGER.info(String.format("Mining partition %d repo(s)", repoPartition.size()));
            try {
                List<Integer> repoIds = repoPartition.stream().map(doc -> (Integer)doc.get("id")).collect(Collectors.toList());
                Map<Integer, Integer> pushMap = countPush(repoIds);
                Map<Integer, Integer> releaseMap = countRelease(repoIds);
                Map<Integer, Integer> contributorMap = countContributor(repoIds);

                repoPartition.forEach(doc -> {
                    Integer repoId = (Integer)doc.get("id");
                    Document repoStats = new Document("id", repoId);
                    repoStats.append("push", pushMap.get(repoId));
                    repoStats.append("release", releaseMap.get(repoId));
                    repoStats.append("contributor", contributorMap.get(repoId));

                    gitRepoStatsRepo.upsert(repoStats);
                });

            } catch (Exception e) {
                LOGGER.error(e);
                return String.format("Mining failed. Error: %s", e.getMessage());
            }
            return String.format("Mining %d repo(s) successfully", repoPartition.size());
        };
    }

    public Map<Integer, Integer> countPush(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        List<Document> pushCountResult = pushEventRepo.countPushByRepoIds(repoIds);

        Map<Integer, Integer> retMap = new HashMap<>();
        pushCountResult.forEach(doc -> {
            Integer repoId = (Integer) doc.get("_id");
            Integer count = (Integer) doc.get("count");
            retMap.put(repoId, count);
        });
        // set 0 for the repo doesn't have the count
        repoIds.forEach(repoId -> {
            if (!retMap.containsKey(repoId)) {
                retMap.put(repoId, 0);
            }
        });
        return retMap;
    }

    public Map<Integer, Integer> countRelease(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        List<Document> releaseCountResult = releaseEventRepo.countReleaseByRepoIds(repoIds);

        Map<Integer, Integer> retMap = new HashMap<>();
        releaseCountResult.forEach(doc -> {
            Integer repoId = (Integer) doc.get("_id");
            Integer count = (Integer) doc.get("count");
            retMap.put(repoId, count);
        });
        // set 0 for the repo doesn't have the count
        repoIds.forEach(repoId -> {
            if (!retMap.containsKey(repoId)) {
                retMap.put(repoId, 0);
            }
        });
        return retMap;
    }

    public Map<Integer, Integer> countContributor(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        List<Document> contributorCountResult = forkEventRepo.countForkByRepoIds(repoIds);
        Map<Integer, Integer> retMap = new HashMap<>();
        contributorCountResult.forEach(doc -> {
            Integer repoId = (Integer) doc.get("_id");
            Integer count = (Integer) doc.get("count");
            retMap.put(repoId, count);
        });
        // set 0 for the repo doesn't have the count
        repoIds.forEach(repoId -> {
            if (!retMap.containsKey(repoId)) {
                retMap.put(repoId, 0);
            }
        });
        return retMap;
    }
}
