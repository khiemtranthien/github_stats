package com.ttk;

import com.ttk.report.Top1000HealthiesRepoReport;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class.getName());

    public static void main(String[] args) {

        try {
            new Top1000HealthiesRepoReport().run();

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}
