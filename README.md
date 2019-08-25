Project implementation
---
* Extract JSON files, store needed event types to different MonggoDB collections (ex: PushEvent -> pushEvent collection)
    * If store all events in one collection, it takes too long to query and it may overflow memory sometimes 

* There is a collection stores repo's info which will be used to loop for each to get its statistic (ex total pushes, total release, etc per repo)
    * Repo info is extracted from PushEvent. A repo doesn't have any push will have no statistic
    * Use repoId (in where statement) to collect its statistic instead of running a query group by repo.id is 
    because it's take long time to run the query and transfer the data to Java process (there are about 2.7 million repos)

* There is a collection to store repo's statistic. It's a base collection used to calculate repo health

* CSV report will get health score from repo statistic collection, sort and export to file

Health score formula
--- 

health_score = push_count/max_push * release_count/max_release * contributor_count/max_contributor

Setup & run
---
1. Clone project, open app.properties, create **zip**, **download** and **json** folder
2. Use: wget https://data.gharchive.org/2019-08-18-{0..23}.json.gz
3. Download data for last 30 days (ex: 2019-07-20 to 2019-08-20) to **zip** folder
4. Can use download_archive_files.sh
5. Ingest event data to DB:
    * Update *datasource/EventTypeIngestor.java* **main** function the eventType and the date range to ingest
    * Note: don't set the range too long, json file extracted will consume all your disk space
    
6. After the ingest data complete, create index for each collection in Monggo DB
7. Update date range in **main** and run *datasource/GitRepoIngestor.java* to ingest Git Repo info 
8. Run *mining/GitRepoStatsMiner.java* to mining repo statistic like total push, release, contributor, etc
9. Run *mining/RepoHealthMiner.java* to calculate repo health score
10. Run *report/RepoHealthReport.java* to get top healthiest repos in CSV format (change the size in *app.properties* file)

Tech stack
---
* MongoDB v4.0.3
* Java 8

Technical decisions
--
* Why to choose MongoDB:
    * Flexible schema helps me to adapt with a number of different event payload structures
    * Be a good choice to store JSON-like data
    * Easy to setup and fit with my laptop storage and RAM
    
* Why is Java 8:
    * Java 7 is deprecated soon
    * Java 8 have big improvements on code syntax: lambda, stream, 
        instant date, concurrency (executor), javascript engine (nashorn), etc, which helps me code more quickly
    

Need to improve
---
* Add unit test
* Consider restructure class implementation with interface to support mock test
* Add dependency injection to manage repo objects to de-coupling between classes
* Move some hard-code number/string to config file