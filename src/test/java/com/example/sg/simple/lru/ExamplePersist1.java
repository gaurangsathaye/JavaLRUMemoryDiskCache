package com.example.sg.simple.lru;

import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class ExamplePersist1 {
    private static final String dataDirectory ="/Users/sathayeg/projects/0github/JavaSimpleLRUCache/datadir";
    
    public static ExampleCache1 cache;

    public static void main(String[] args) {
        try {
            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache1("ExampleCache1", 10000, new ExampleDao(), true, dataDirectory);
            
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
        for(int i=0;i<1;i++) {
            p("start cache.get");
            cache.get(Integer.toString(i));
            p("done cache.get");
            p(cache.getStats());
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
