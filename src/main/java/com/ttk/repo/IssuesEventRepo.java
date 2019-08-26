package com.ttk.repo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import javax.script.ScriptException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IssuesEventRepo extends MongoBaseRepo {
    private static final Logger LOGGER = LogManager.getLogger(IssuesEventRepo.class.getName());

    public IssuesEventRepo() {
        super();
        collection = database.getCollection("issuesEvent");
    }

    public List<Document> getOpenedIssueCreatedDate(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        params.put("repoIds:arrayNum", repoIds);

        List<Document> pipeline = parseQueryString("issues.js", "IssueOpenedCreated", params);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);
        return result;
    }

    public List<Document> getNonOpenedIssueMinCreatedDate(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        Map<String, Object> params = new HashMap<>();
        params.put("repoIds:arrayNum", repoIds);

        List<Document> pipeline = parseQueryString("issues.js", "IssueNonOpenedCreated", params);

        LOGGER.debug(String.format("Pipeline %s", pipeline));

        List<Document> result = aggregation(pipeline);
        return result;
    }

    public Map<Integer, Double> getAverageIssueStayOpened(List<Integer> repoIds) throws ScriptException, NoSuchMethodException {
        /*
            1. get the time the issue is opened
            2. get the time the issue change its status to anther status (non-opened)
            3. calculate the time between
            4. average remain open time for each repo
         */
        List<Document> openedIssues = getOpenedIssueCreatedDate(repoIds);
        List<Document> nonOpenedIssues = getNonOpenedIssueMinCreatedDate(repoIds);

        Function<Document, String> generateKey = item -> {
            Document _idDoc = (Document)item.get("_id");
            String key = String.format("%d_%d", _idDoc.getInteger("repo"), _idDoc.getInteger("issue"));
            return key;
        };

        Map<String, Date> openedIssueDateMap = openedIssues.stream().collect(
                Collectors.toMap(generateKey, item -> item.getDate("date")));

        Map<String, Date> nonOpenedIssueDateMap = nonOpenedIssues.stream().collect(
                Collectors.toMap(generateKey, item -> item.getDate("minDate")));

        // if the issue still in open status, set the end time to current time
        Map<String, Long> remainOpenTimeMap = new HashMap<>();
        openedIssueDateMap.forEach((k, v) -> {
            // if the issue still in open status, set the end time to current time
            Date endOpen = new Date();

            if (nonOpenedIssueDateMap.containsKey(k)) {
                endOpen = nonOpenedIssueDateMap.get(k);
            }

            if(endOpen.before(v)) {
                throw new IllegalStateException("Issue non open time invalid");
            }

            long openTimeMinutes = (endOpen.getTime() - v.getTime()) / 60000;

            remainOpenTimeMap.put(k, openTimeMinutes);
        });

        Map<Integer, List<Long>> repoIssuesMap = new HashMap<>();
        remainOpenTimeMap.forEach((k, v) -> {
            String[] pair = k.split("_");
            Integer repoId = Integer.valueOf(pair[0]);
            if(!repoIssuesMap.containsKey(repoId)) {
                repoIssuesMap.put(repoId, new ArrayList<>());
            }
            repoIssuesMap.get(repoId).add(v);
        });

        Map<Integer, Double> repoAverageOpenTime = new HashMap<>();
        repoIssuesMap.forEach((k, v) -> {
            double average = v.stream().collect(Collectors.averagingLong(Long::longValue));
            repoAverageOpenTime.put(k, average);
        });

        return repoAverageOpenTime;
    }

    public static void main(String[] args) throws ScriptException, NoSuchMethodException {
//        List<Document> result = new IssuesEventRepo().getOpenedIssueCreatedDate(Arrays.asList(20313056, 153640774));
//        List<Document> result = new IssuesEventRepo().getNonOpenedIssueMinCreatedDate(Arrays.asList(20313056, 153640774));
        Map<Integer, Double> result = new IssuesEventRepo().getAverageIssueStayOpened(Arrays.asList(20313056, 153640774));
        LOGGER.info(result);
    }
}
