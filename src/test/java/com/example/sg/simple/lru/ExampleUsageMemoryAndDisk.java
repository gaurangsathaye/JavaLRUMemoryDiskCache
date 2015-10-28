package com.example.sg.simple.lru;

import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageMemoryAndDisk {
    private static final String dataDirectory ="./datadir";
    
    public static ExampleCache1 cache;

    public static void main(String[] args) {
        try {
            long start1 = System.currentTimeMillis();
            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache1("ExampleCache1", 10000, new ExampleDao(), true, dataDirectory);
            p("Cache creation time: " + (System.currentTimeMillis() - start1));
            
            long start2 = System.currentTimeMillis();
            //Use the cache
            runExample();
            p("run time: " + (System.currentTimeMillis() - start2));
            
        } catch (Exception e) {
            p("Error: " + e);
        }
    }

    /*
     Create the cache with the cache name and the number of items you want to keep in the cache.
     */
    static void runExample() throws Exception {
        for(int i=0;i<12000;i++) {
            cache.get(Integer.toString(new Random().nextInt(15000)));            
        }
        p(cache.getStats());
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
