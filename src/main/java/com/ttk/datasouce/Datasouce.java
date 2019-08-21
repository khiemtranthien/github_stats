package com.ttk.datasouce;

import com.ttk.Main;
import com.ttk.utils.AppProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Datasouce {
    private static final Logger LOGGER = LogManager.getLogger(Datasouce.class.getName());

    /*
    Assure json files are downloaded completely for config date range
     */
    public static void assureValid() throws Exception {
        List<String> neededFiles = generateJsonFileNamesFromConfigDateRange();
        LOGGER.debug("Needed files: " + neededFiles);

        List<String> jsonFiles = getAllJsonFileNames();
        LOGGER.debug("Available files: " + jsonFiles);

        List<String> missingFiles = neededFiles
                .stream()
                .filter(fileName -> !jsonFiles.contains(fileName))
                .collect(Collectors.toList());

        if(missingFiles.size() > 0) {
            LOGGER.info(String.format("The datasouce is missing. Miss: %s file(s). Navigate to data folder, open the terminal and run following command(s):", missingFiles.size()));
            missingFiles.stream().map(fileName -> String.format("wget https://data.gharchive.org/%s.gz", fileName)).forEach(LOGGER::info);

            throw new Exception("Datasource is invalid");
        } else {
            LOGGER.info("Datasouce is OK");
        }
    }

    public static List<String> generateJsonFileNamesFromConfigDateRange() {
        AppProperties appConfig = AppProperties.getInstance();

        DateTimeFormatter simpleFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String configDateFrom = appConfig.get("data.dateFrom");
        String configDateTo = appConfig.get("data.dateTo");

        LocalDateTime dateSince = LocalDate.parse(configDateFrom, simpleFmt).atStartOfDay();
        LocalDateTime dateUntil = LocalDate.parse(configDateTo, simpleFmt).atStartOfDay();

        List<String> fileNames = new ArrayList<>();
        while (dateSince.isBefore(dateUntil)) {
            String fileName = String.format("%d-%02d-%02d-%d.json",
                    dateSince.getYear(), dateSince.getMonth().getValue(),
                    dateSince.getDayOfMonth(), dateSince.getHour());
            fileNames.add(fileName);

            dateSince = dateSince.plus(1, ChronoUnit.HOURS);
        }
        return fileNames;
    }

    /*
    Get json files from data folder
     */
    public static List<String> getAllJsonFileNames() throws IOException {
        AppProperties appConfig = AppProperties.getInstance();
        String dataFolder = appConfig.get("data.folder");

        Stream<Path> stream = Files.list(Paths.get(dataFolder));
        List<String> jsonFiles = stream
                .filter(path -> path.toFile().isFile())
                .map(String::valueOf)
                .filter(path -> path.endsWith(".json"))
                .map(path -> path.substring(path.lastIndexOf('/') + 1))
                .collect(Collectors.toList());

        return jsonFiles;
    }

    /*
    Get json files in config date range (for ingestion eg)
     */
    public static List<String> getDesiredJsonFilesForIngestion() throws Exception {
        assureValid();

        AppProperties appConfig = AppProperties.getInstance();
        String dataFolder = appConfig.get("data.folder");

        List<String> fileNames = generateJsonFileNamesFromConfigDateRange();
        List<String> filePaths = fileNames.stream().map(name -> dataFolder + name).collect(Collectors.toList());
        return filePaths;
    }

}
