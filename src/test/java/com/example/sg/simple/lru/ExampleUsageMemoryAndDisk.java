package com.example.sg.simple.lru;

import java.io.File;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageMemoryAndDisk {

    private static final String dataDirectory = "./datadir";

    public static ExampleCache1 cache;

    public static void main(String[] args) {
        try {
            File datadir = new File(dataDirectory);
            if (datadir.exists()) {
                p("Warning: First delete the data directory: " + dataDirectory + ", for this test !!!");
                p("The cache will create it if it does not exist");
                return;
            }

            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache1("ExampleCache1", 10, new ExampleDao(), dataDirectory);

            //Use the cache
            runExample();
        } catch (Exception e) {
            p("Error: " + e + " ::: cause: " + e.getCause().toString());
        }
    }

    /*
     Create the cache with the cache name and the number of items you want to keep in the cache.
     */
    static void runExample() throws Exception {
        String key = "testKey";

        p("Nothing on disk and memory at this call, key object retrieved from 'loadData' call");
        cache.get(key);
        printHitMissStats(cache.getStats());
        p(" --- \n");

        p("Key object retrieved from memory");
        cache.get(key);
        printHitMissStats(cache.getStats());
        p(" --- \n");

        p("Clear cache to simulate your process shutting down");
        p("Key object is on disk only, not in memory");
        cache.clear();
        p(" --- \n");

        p("Key object retrieved from disk");
        cache.get(key);
        printHitMissStats(cache.getStats());
        p(" --- \n");

        p("Key object retrieved from memory");
        cache.get(key);
        printHitMissStats(cache.getStats());
        p(" --- \n");
    }

    static void printHitMissStats(Map<String, Object> map) throws Exception {
        long totalHits = (Long) map.get("hits");
        long hitsDisk = (Long) map.get("hitsDisk");
        long hitsMemory = (Long) map.get("hitsMemory");
        long misses = (Long) map.get("misses");
        p("totalHits: " + totalHits + ", hitsDisk: " + hitsDisk + ", hitsMemory: " + hitsMemory + ", misses: " + misses);
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
