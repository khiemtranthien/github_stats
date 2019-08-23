package com.ttk.mining;

import com.ttk.datasouce.GitRepoIngestor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GitRepoMiner {
    private static final Logger LOGGER = LogManager.getLogger(GitRepoMiner.class.getName());

    public static void main(String[] args) {
        String dateFrom = "2019-07-21";
        String dateTo = "2019-08-20";

        new GitRepoIngestor().run(dateFrom, dateTo);
    }
}
