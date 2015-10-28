package com.example.lru.memory.disk.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageMemoryAndDisk2 {

    private static final String dataDirectory = "./datadir";

    public static ExampleCache1 cache;

    public static void main(String[] args) {
        try {
            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache1("ExampleCache1", 50000, new ExampleDao(), dataDirectory);

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
        ExecutorService pool = Executors.newFixedThreadPool(500);
        
        List<Future<String>> list = new ArrayList<Future<String>>();
        
        for(int i=0;i<10000;i++){
            final int id = i;
            Future<String> future = pool.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int random = new Random().nextInt(70000);
                    cache.get(Integer.toString(random));
                    if(random % 500 == 0){
                        p(cache.getStats());
                    }
                    return null;
                }
            });
            list.add(future);
        }
        
        for(Future<String> f : list){
            f.get();
        }
        
        p("final stats: " + cache.getStats());
        
        try{pool.shutdown();}catch(Exception e){}
        try{pool.shutdownNow();}catch(Exception e){}
        System.exit(0);
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
