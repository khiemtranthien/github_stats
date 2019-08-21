#!/usr/bin/env bash

BASE_FOLDER=/Users/khiemtran/GHArchive

cd "$BASE_FOLDER/download/"

#wget https://data.gharchive.org/2019-08-15-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-14-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-13-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-12-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-11-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-10-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-09-{0..23}.json.gz

#wget https://data.gharchive.org/2019-08-08-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-07-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-06-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-05-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-04-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-03-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-02-{0..23}.json.gz
#wget https://data.gharchive.org/2019-08-01-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-31-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-30-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-29-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-28-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-27-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-26-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-25-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-24-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-23-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-22-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-21-{0..23}.json.gz
#wget https://data.gharchive.org/2019-07-20-{0..23}.json.gz


cp *.gz "$BASE_FOLDER/zip/"

gunzip *.gz

mv *.json "$BASE_FOLDER/json/"