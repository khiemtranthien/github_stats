package com.ttk.report;

import com.ttk.repo.GitRepoRepo;
import com.ttk.repo.GitRepoStatsRepo;
import com.ttk.utils.AppProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Top1000HealthiesRepoReport {
    private static final Logger LOGGER = LogManager.getLogger(Top1000HealthiesRepoReport.class.getName());

    GitRepoStatsRepo gitRepoStatsRepo;
    GitRepoRepo gitRepoRepo;

    public Top1000HealthiesRepoReport() {

        this.gitRepoStatsRepo = new GitRepoStatsRepo();
        this.gitRepoRepo = new GitRepoRepo();
    }

    public static void main(String[] args) {
        try {
            new Top1000HealthiesRepoReport().run();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void run() throws IOException {
        AppProperties appConfig = AppProperties.getInstance();
        Integer topSize = Integer.valueOf(appConfig.get("report.topRepoSize"));

        List<Document> topRepos = gitRepoStatsRepo.getTopHealthiestRepo(topSize);

        LOGGER.info(String.format("Top healthiest repos size: %d", topRepos.size()));

        List<Integer> repoIds = new ArrayList<>();
        topRepos.forEach(repo -> {
            Integer repoId = repo.getInteger("id");
            repoIds.add(repoId);
        });

        List<Document> repoInfos = gitRepoRepo.getRepoInfoByIds(repoIds);
        Map<Integer, Document> repoInfosMap = repoInfos.stream().collect(
                Collectors.toMap(repoInfo -> repoInfo.getInteger("id"), repoInfo -> repoInfo));

        LOGGER.info(String.format("Repo infos size: %d", repoInfos.size()));

        List<String[]> csvData = topRepos.stream().map(repo -> {
            Integer repoId = repo.getInteger("id");
            Document repoInfo = repoInfosMap.get(repoId);
            String repoName = extractRepoName(repoInfo.getString("name"));
            String orgName = "";
            if (repoInfo.containsKey("org")) {
                Document org = (Document)repoInfo.get("org");
                orgName = org.getString("login");
            }

            Integer pushCount = repo.getInteger("push");
            Integer releaseCount = repo.getInteger("release");
            Integer contributorCount = repo.getInteger("contributor");
            Double issueOpenedAvg = repo.getDouble("issue_opened_avg");

            Double healthScore = repo.getDouble("health_score");

            String[] rowData = new String[] {
                    orgName,
                    repoName,
                    String.valueOf(healthScore),
                    String.valueOf(pushCount),
                    String.valueOf(releaseCount),
                    String.valueOf(contributorCount),
                    String.valueOf(issueOpenedAvg)
            };
            return rowData;

        }).collect(Collectors.toList());

        writeToCsvFile(csvData);

        LOGGER.info("Done");
    }

    private void writeToCsvFile(List<String[]> csvData) throws IOException {
        CSVWriter writer = new CSVWriter();
        AppProperties appConfig = AppProperties.getInstance();
        String reportFolder = appConfig.get("report.folder");

        String filePath = reportFolder + "top1000Repo.csv";

        LOGGER.info(String.format("CSV output path: %s", filePath));
        LOGGER.info(String.format("Write %d row(s)", csvData.size()));

        String[] header = new String[] {"Org", "RepoName", "HealthScore", "Commits", "Releases", "Contributors", "IssuseRemainOpen"};
        writer.writeToFile(filePath, csvData, header);
    }

    private String extractRepoName(String fullName) {
        if(fullName.indexOf('/') > -1) {
            String[] parts = fullName.split("/");
            return parts[parts.length - 1];
        } else {
            return fullName;
        }
    }

}
