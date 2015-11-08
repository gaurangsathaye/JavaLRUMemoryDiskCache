package com.test.lru.memory.disk.cache.dist;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.Distributor;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sathayeg
 */
public class TDistributedThreads {

    public static void main(String[] args) {
        try {
            TDistributedThreads td = new TDistributedThreads();
            td.distribute();
        } catch (Exception e) {
            p("err: " + e + " : cause: " + e.getCause());
            System.exit(0);
        }
    }
    
    static int cacheSize = 1000;
    static int loopCount = 10;
    static int randomRange = 2;
    static int threadPoolSize = 5;
    void distribute() throws Exception {
        p("start distribute");
        Cache cache1 = new Cache("teaCache", cacheSize, true, "./datadir/server1/teacache", new Dao("server1"));
        Cache cache2 = new Cache("teaCache", cacheSize, true, "./datadir/server2/teacache", new Dao("server2"));
        Cache cache3 = new Cache("coffeeCache", cacheSize, true, "./datadir/server1/coffeecache", new Dao("server1"));
        Cache cache4 = new Cache("coffeeCache", cacheSize, true, "./datadir/server2/coffeecache", new Dao("server2"));

        List<Cache> cacheList = new ArrayList<>();
        cacheList.add(cache1);
        cacheList.add(cache2);
        //cacheList.add(cache3);
        //cacheList.add(cache4);

        String clusterConfig = "127.0.0.1:19000, 127.0.0.1:19001";

        Distributor.config(220, 7000, 15000);
        Distributor.distribute(19000, clusterConfig, cache1, cache3);
        Distributor.distribute(19001, clusterConfig, cache2, cache4);
        
        try{Thread.sleep(3000);}catch(Exception e){}
        
        ExecutorService execService = Executors.newFixedThreadPool(threadPoolSize);
        AtomicInteger ai = new AtomicInteger(0);
        int cacheListSize = cacheList.size();
        int totalCount = loopCount * cacheListSize;
        p("totalCount: " + totalCount);

        for (int a = 0; a < loopCount; a++) {
            for (Cache cache : cacheList) {
                execService.execute(new CacheGetThread(cache, ai));
            }
        }
        
        while(true){
            if(ai.get() >= totalCount){
                for(Cache cache : cacheList){
                    p(cache.getStats() + "\n");
                }
                try{Thread.sleep(10000);}catch(Exception e){}
                System.exit(0);
            }else{
                p("not done yet: done: " + ai.get() + ", totalCount: " + totalCount);
                try{Thread.sleep(2000);}catch(Exception e){}
            }            
        }
    }

    private class CacheGetThread implements Runnable {
        private final Cache cache;
        private final AtomicInteger ai;

        public CacheGetThread(Cache cache, AtomicInteger ai) {
            this.cache = cache;
            this.ai=ai;
        }

        @Override
        public void run() {
            try {
                try{
                    Thread.sleep(new Random().nextInt(800) + 200);
                }catch(Exception e){}
                StringBuilder sb = new StringBuilder();
                String val = cache.get(Integer.toString(new Random().nextInt(randomRange)));
                sb.append(val).append("\n").append("\n===============\n");
                p(sb.toString());
            } catch (Exception e) {
                p("error CacheGetThread: " + cache.getCacheName() + " : " + e);
            }
            ai.incrementAndGet();
        }
    }

    class Cache extends AbstractCacheService<String> {
        private final Dao dao;

        Cache(String cacheName, int cacheSize, Dao dao) throws Exception {
            super(cacheName, cacheSize);
            this.dao = dao;
        }
        
        Cache(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory, Dao dao) throws Exception {
            super(cacheName, cacheSize, diskPersist, dataDirectory);
            this.dao = dao;
        }

        @Override
        public boolean isCacheItemValid(String o) {
            return (null != o);
        }

        @Override
        public String loadData(String key) throws Exception {
            return this.dao.getData(key, this.getCacheName());
        }

    }

    class Dao {
        private final String server;

        public Dao(String server) {
            this.server = server;
        }

        public String getData(String key, String cacheName) {
            return "data for key: " + key + ", cacheName: " + cacheName + ", server: " + server;
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
