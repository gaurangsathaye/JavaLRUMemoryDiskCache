package com.example.sg.simple.lru;

import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class Example1 {
    
    public static ExampleCache1 cache;

    public static void main(String[] args) {
        try {
            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache1("ExampleCache1", 10000, new ExampleDao());
            
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
            cache.get(Integer.toString(new Random().nextInt(200)));
            p(cache.getStats());
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
