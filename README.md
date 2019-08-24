Project implementation
---

Github repository is extracted from PushEvent. A repo doesn't have any push will have no statistic

Health score formula
--- 

Tech stack
---
* MongoDB v4.0.3
* Java 8

Setup
---
1. Clone project, open app.properties, create **zip**, **download** and **json** folder
2. Use: wget https://data.gharchive.org/2015-01-01-{0..23}.json.gz
3. Download data for last 30 days (ex: 2019-07-20 to 2019-08-20) to **zip** folder
4. Can use download_archive_files.sh
5. Ingest event data to DB:
    * Update EventTypeIngestor **main** function the eventType and the date range to ingest
    * Note: don't set the range too long, json file extracted will consume all your disk space
    
6. After the ingest data complete, create index for each collection in Monggo DB
7. Update date range and run datasource/GitRepoIngestor.java to ingest Git Repo info 
8. Run mining/GitRepoStatsMiner.java to mining repo statistic like total push, release, contributor, etc
9. Run mining/RepoHealthMiner.java to calculate repo health score
10. Run report/RepoHealthReport to get top 1000 healthiest repos in CSV format

Need to improve
---
* Add unit test
* Consider restructure class implementation with interface to support mock test 