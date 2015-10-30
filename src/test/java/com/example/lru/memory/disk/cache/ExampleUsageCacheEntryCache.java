package com.example.lru.memory.disk.cache;

import com.lru.memory.disk.cache.CacheEntry;

/**
 *
 * @author sathayeg
 */
public class ExampleUsageCacheEntryCache {
    
    static ExampleCacheEntryCache cache = null;
    
    public static void main(String[] args){
        try{
            cache = new ExampleCacheEntryCache("CacheEntryCache", 1000, true, "./dataDirCacheEntryCache");
            test();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void test() throws Exception {        
        CacheEntry<ExampleObjectToCache> ceNull = cache.get("nullObject");
        p("ceNull: " + ceNull.getCached() + ": cache entry timestamp: " + ceNull.getTimestamp());
        
        CacheEntry<ExampleObjectToCache> ceObj = cache.get("id123");
        p("ceObj: " + ceObj.getCached().getData() + ": cache entry timestamp: " + ceObj.getTimestamp());
        
        try{Thread.sleep(6000);}catch(Exception e){} //cache entry expires after 5 seconds
        
        p("After 5 seconds the object for key: id123 is reloaded. See the cache entry timestamp is now different.");
        ceObj = cache.get("id123");
        p("ceObj: " + ceObj.getCached().getData() + ": cache entry timestamp: " + ceObj.getTimestamp());
        
        p("stats: " + cache.getStats());
        p("disk location of ceObj: " + cache.getPathToFile("id123"));
    }
    
    static void p(Object o){
        System.out.println(o);
    }
}
