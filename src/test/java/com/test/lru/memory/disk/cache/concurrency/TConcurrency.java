package com.test.lru.memory.disk.cache.concurrency;

import com.lru.memory.disk.cache.AbstractCacheService;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 */
public class TConcurrency {
    public static void main(String[] args){
        try{
            TConcurrency tc = new TConcurrency();
            tc.t1();
            //tReentrant();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    /*
        Test long loadData time for a given key
    */
    void t1() throws Exception {
        Cache cache = new Cache("cache", 100, new StringLoader());
        new Thread(() -> {
            try {
                p("cache get sleep: " + cache.get("sleep"));
            } catch (Exception e) {
                p("error get a: " + e);
            }
        }).start();
        
        try{Thread.sleep(400);}catch(Exception e){}
        
        new Thread(() -> {
            try {
                p("cache get b: " + cache.get("b"));
            } catch (Exception e) {
                p("error get b: " + e);
            }
        }).start();
    }
    
    static void p(Object o){
        System.out.println(o);
    }
    
    private class Cache extends AbstractCacheService<String>{
        private final StringLoader sl;
        public Cache(String cacheName, int cacheSize, StringLoader sl) throws Exception {
            super(cacheName, cacheSize);
            this.sl = sl;
        }

        @Override
        public boolean isCacheItemValid(String o) {
            return (null != o);
        }

        @Override
        public String loadData(String key) throws Exception {
            return sl.load(key);
        }   
    }
    
    private class StringLoader {
        public String load(String key){
            if(key.equals("sleep")){
                p("key is sleep");
                long start = System.currentTimeMillis();
                try{Thread.sleep(3000);}catch(Exception e){}
                //p("key sleep end: " + (System.currentTimeMillis() - start));
            }            
            return "data:" + key;
        }
    }
}
