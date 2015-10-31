package com.example.lru.memory.disk.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageMemoryAndDisk2 {

    private static final String dataDirectory = "./datadir";

    public static ExampleCache cache;

    public static void main(String[] args) {
        try {
            //For example, create cache that can be accessed by all parts of your code.
            cache = new ExampleCache("ExampleCache1", 50000, true, dataDirectory, new ExampleDao());

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
        ExecutorService pool = Executors.newFixedThreadPool(300);
        
        final Map<String, AtomicInteger> map  = new HashMap<>();
        map.put("ct", new AtomicInteger(0));
        
        int total = 10000;
        
        
        for(int i=0;i<total;i++){
            final int id = i;
            pool.submit((new Callable<String>() {
                @Override
                public String call() throws Exception {
                    int random = new Random().nextInt(70000);
                    String key = Integer.toString(random);
                    long start = System.currentTimeMillis();
                    long end = 0L;
                    try{
                        //p("pre cache get: " + key);
                        cache.get(key);
                        end = System.currentTimeMillis() - start;
                        //p("post cache get: " + key);
                    }catch(Exception e){
                        p("Fatal: " + e + " : cause: " + e.getCause());
                    }
                    if((id % 500) == 0){
                        p("time: " + end + ", done:" + id + " : " + key + " : " + cache.getStats() + cache.getPathToFile(key));
                    }
                    map.get("ct").incrementAndGet();
                    return null;
                }
            }));
        }
        
        while(true){
            p("ct: " + map.get("ct").get());
            if(map.get("ct").get() >= total){
                System.exit(0);
            }
            try{Thread.sleep(2000);}catch(Exception e){}
        }
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
