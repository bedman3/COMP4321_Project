# COMP4321 Search Engine Project Group 7

## Build instructions

You need to first [install maven](https://maven.apache.org/install.html) (`sudo apt install maven` in Ubuntu) and run this program in linux (only tested in Ubuntu, not sure about Window).

## How to run spider and test program?

They are all written in two test case, in order to run both of them together, do `mvn -Dtest=MainTests test`. A text file `spider_result.txt` should appear in your project root directory.

The test case can be found under `src/test/java/com/comp4321Project/searchEngine/MainTests.java`

```java
class MainTests {
    @BeforeAll
    public static void scrapeUrlToRocksDB() {
        try {
            String url = "http://www.cse.ust.hk";
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            Spider spider = new SpiderImpl(rocksDBDao, 5);
            spider.scrape(url, true, 30);

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadResultFromRocksDB() {
        try {
            String outputFileName = "spider_result.txt";
            String dashedLineSeparator = "--------------------------------------------------------------------------";
            File outputFile = new File(outputFileName);

            if (outputFile.exists()) {
                outputFile.delete();
            }
            outputFile.createNewFile();

            PrintWriter printWriter = new PrintWriter(outputFile);

            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            QuerySearch querySearch = new QuerySearch(rocksDBDao);

            List<SiteMetaData> resultsList = querySearch.getAllSiteFromDB();
            resultsList.forEach(siteMetaData -> {
                printWriter.println(siteMetaData.toPrint());
                printWriter.println(dashedLineSeparator);
            });

            rocksDBDao.getRocksDB().closeE();
        } catch (RocksDBException | NullPointerException | IOException e) {
            e.printStackTrace();
        }
    }

    // debug code
    /*@AfterAll
    static void printAllDataInRocksDB() {
        try {
            RocksDBDao rocksDBDao = new RocksDBDaoImpl();
            rocksDBDao.printAllDataInRocksDB();
        } catch (RocksDBException | NullPointerException e) {
            e.printStackTrace();
        }
    }*/
}
```

If you would like to execute the test separately, for spider run: `mvn -Dtest=SeparateTest#scrapeUrlToRocksDB test`,
for test program run: `mvn -Dtest=SeparateTest#loadResultFromRocksDB test`
