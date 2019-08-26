Project implementation
---
* Extract JSON files, store needed event types to different MonggoDB collections (ex: PushEvent -> pushEvent collection)
    * If store all events in one collection, it takes too long to query and it may overflow memory 
    * Note: should run for a small date range because the ingestion also take long time

* Github repo's info is stored in a new collection, which will be used to collect its statistic (ex total pushes, total release, etc)
    * Repo info is extracted from PushEvent. A repo doesn't have any push will have no statistic
    
* Repo's statistic is also stored in a new collection. It's a base collection used to calculate repo health and generate the report
    * Use repoId (in where statement) to collect its statistic from event collections instead of running a query group by repo.id,
    because it takes long time to run the query and transfer the data to Java process (there are about 2.7 million repos)


* The idea for implementing this project is break down big task to smaller tasks
    * One task consumes less memory, disk space and takes shorter time to finish
    * Running independent to each other (can re-run at any step), be testable
    * Whole process may take longer to complete but it's monitorable, we know that it's still running well

Health score formula
--- 

health_score = 
    push_count / max_push * 
    release_count / max_release * 
    contributor_count / max_contributor *
    issue_open_time / min_open_time

Setup & run
---
1. Clone project, open *app.properties* in **resources** folder, create **zip**, **download** and **json** folder
2. Use: wget https://data.gharchive.org/2019-08-18-{0..23}.json.gz to download data
3. Download data for last 30 days (ex: 2019-07-20 to 2019-08-20) to **zip** folder
4. Can use download_archive_files.sh
5. Ingest event data to DB:
    * Update dateFrom and dateTo in *app.properties file* 
    * Run *datasource/EventTypeIngestor.java* to run the ingestion
    
6. After the ingest data complete, **create index** for each collection in Monggo DB. Refer: *mongodb_scripts/monggo_ddl.js*
7. Run *datasource/GitRepoIngestor.java* to collect Git repo's info 
8. Run *mining/GitRepoStatsMiner.java* to mining repo statistic like total push, release, contributor, etc
9. Run *mining/RepoHealthMiner.java* to calculate repo health score
10. Run *report/RepoHealthReport.java* to generate top healthiest repos report in CSV format. Check result in **csv_reports** folder

Tech stack
---
* MongoDB v4.0.3
* Java 8

Technical decisions
--
* Why to choose MongoDB:
    * Flexible schema adapts to a number of different event payload structures easily
    * Be a good choice to store JSON-like data
    * Easy to setup and fit with my laptop storage and memory
    
* Why is Java 8:
    * Java 7 is deprecated soon
    * Java 8 have big improvements on code syntax: lambda, stream, 
        instant date, concurrency (executor), javascript engine (nashorn), etc, which helps me code more quickly and efficiently
    

Need to improve
---
* Features:
    * Add more metric to make the health score more sense
    
* Code:
    * Add unit test
    * Consider restructure class implementation with interface to support mock test
    * Add dependency injection to manage repo objects to de-coupling between classes