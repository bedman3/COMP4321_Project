# COMP4321 Search Engine Project Group 7

## Build instructions

#### 1. If you have no `Docker` and `Docker Compose`

You need to first [install maven](https://maven.apache.org/install.html) (`sudo apt install maven` in Ubuntu) 
and run this program in linux (only tested in Ubuntu, not sure about Window). 
Then [install python 3.x](https://docs.conda.io/en/latest/miniconda.html) and 
[install the newest yarn](https://yarnpkg.com/getting-started/install).

```shell script
# command to type

```

or you can install `Docker` from [here](https://docs.docker.com/get-docker/) and 
install `Docker Compose` from [here](https://docs.docker.com/compose/install/#install-compose) 
then go to 2.

#### 2. If you have `Docker` and `Docker Compose`

Choose a path where you store your RocksDB Files (or load the existing RocksDB files)

```shell script
# run the folloing script
# you can try "~" here (put RocksDB files in your home directory), if you put the RocksDB files 
# under /A/B/C/rocksDBFiles, then you put "/A/B/C" in <ENTER_PATH_HERE>
export ROCKSDB_BASE_DIRECTORY=<ENTER_PATH_HERE>
docker-compose up -d

# if you do not have an existing RocksDB files, the application will generate the files
# you then have to crawl the website data to the database

# to crawl a site recursively with limit, you need to do
# curl -i \
#   -H "Content-Type: application/json" \
#   -XPOST -d '{"url": "<URL_YOU_WANT_TO_CRAWL>", "recursive": true, "limit": 10000}' \
#   http://localhost:8080/crawl

# you can also turn off recursive. if you do not provide a limit, the search engine
# will crawl endlessly until the site has nothing to crawl

# One example of crawl request
curl -i \
  -H "Content-Type: application/json" \
  -XPOST -d '{"url": "www.cse.ust.hk", "recursive": true, "limit": 1000}' \
  http://localhost:8080/crawl

# you can trace the log from web server to see the crawling process is ongoing
# to view the log, do:
# 1) sudo su
# 2) tail -f /var/lib/docker/containers/<BACKEND_CONTAINER_HASH>/<BACKEND_CONTAINER_HASH>-json.log
# examples of logs are:
# {"log":"skip crawling www.cse.ust.hk/~jamesk, parameters -\u003e url: www.cse.ust.hk/~jamesk lastModified: Sat, 02 May 2020 08:43:05 GMT urlId: 190\n","stream":"stderr","time":"2020-05-05T05:11:06.700258195Z"}
# {"log":"Scraping: www.cse.ust.hk/faculty/fred, scraped 998 site(s).\n","stream":"stderr","time":"2020-05-05T05:11:06.700486177Z"}
# {"log":"Scraping: www.cse.ust.hk/pg/admissions/faq, scraped 999 site(s).\n","stream":"stderr","time":"2020-05-05T05:11:06.74865608Z"}
# {"log":"Scraping: www.cse.ust.hk/faculty/dyyeung, scraped 1000 site(s).\n","stream":"stderr","time":"2020-05-05T05:11:07.932178141Z"}

# after the crawling, you need to do batch processing to compute tfidf scroes, page rank scores 
# and do other batch processing. to do this, simply do:
# curl -i -XPOST http://localhost:8080/batch-job

# after the batch job, you should see the following from the log
#
# {"log":"start resetting query response cache\n","stream":"stderr","time":"2020-05-05T05:17:20.139220063Z"}
# {"log":"finished resetting query response cache\n","stream":"stderr","time":"2020-05-05T05:17:20.170765572Z"}
# {"log":"start resetting miscellaneous cache\n","stream":"stderr","time":"2020-05-05T05:17:20.17084633Z"}
# {"log":"finished resetting miscellaneous cache\n","stream":"stderr","time":"2020-05-05T05:17:20.194612335Z"}
# {"log":"start computing idf score for word\n","stream":"stderr","time":"2020-05-05T05:17:20.19465345Z"}
# {"log":"finished computing idf score for word\n","stream":"stderr","time":"2020-05-05T05:17:20.700984753Z"}
# {"log":"start computing tfidf vector\n","stream":"stderr","time":"2020-05-05T05:17:20.701017821Z"}
# {"log":"finished computing tfidf vector\n","stream":"stderr","time":"2020-05-05T05:17:20.893867602Z"}
# {"log":"start computing title vector\n","stream":"stderr","time":"2020-05-05T05:17:20.893894452Z"}
# {"log":"finished computing title vector\n","stream":"stderr","time":"2020-05-05T05:17:20.911949744Z"}
# {"log":"start computing page rank\n","stream":"stderr","time":"2020-05-05T05:17:20.912023008Z"}
# {"log":"start page rank algorithm now\n","stream":"stderr","time":"2020-05-05T05:17:21.784676264Z"}
# {"log":"finished page rank computation\n","stream":"stderr","time":"2020-05-05T05:17:21.784754231Z"}
# {"log":"finished computing page rank\n","stream":"stderr","time":"2020-05-05T05:17:21.840153601Z"}
# {"log":"start computing miscellaneous cache\n","stream":"stderr","time":"2020-05-05T05:17:21.840180355Z"}
# {"log":"start computing stemmed keywords cache\n","stream":"stderr","time":"2020-05-05T05:17:21.840187656Z"}
# {"log":"finished computing stemmed keywords cache\n","stream":"stderr","time":"2020-05-05T05:17:21.853293565Z"}
# {"log":"finished computing miscellaneous cache\n","stream":"stderr","time":"2020-05-05T05:17:21.853317457Z"}

# after finishing the batch processing, you can now query from the frontend interface
# visit localhost:4000 and input your query in the search bar
```
