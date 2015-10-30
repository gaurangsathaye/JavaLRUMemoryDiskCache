package com.example.lru.memory.disk.cache;

import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageMemoryOnly {
    
    public static ExampleCache cache;

    public static void main(String[] args) {
        try {
            //Create cache that can be accessed by all parts of your code.
            cache = new ExampleCache("ExampleCache1", 10000, new ExampleDao());
            p("created cache");
            
            //Use the cache
            runExample();
            
        } catch (Exception e) {
            p("Error: " + e);
        }
    }

    /*
     Create the cache with the cache name and the number of items you want to keep in the cache.
     */
    static void runExample() throws Exception {
        for(int i=0;i<1000;i++) {
            ExampleObjectToCache cachedObject = cache.get(Integer.toString(new Random().nextInt(200)));
            doSomethingWithCachedObject(cachedObject);
        }
        p(cache.getStats());
    }

    static void p(Object o) {
        System.out.println(o);
    }

    private static void doSomethingWithCachedObject(ExampleObjectToCache cachedObject) {
        //do something with your returned cached object
    }
}
