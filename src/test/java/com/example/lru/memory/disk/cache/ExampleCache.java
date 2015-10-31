package com.example.lru.memory.disk.cache;

import com.lru.memory.disk.cache.AbstractCacheService;

/**
 *
 * @author sathayeg
 */
public class ExampleCache extends AbstractCacheService<ExampleObjectToCache>{   
    private final ExampleDao exampleDao;
    
    //Example of constructor that creates an in memory cache only
    public ExampleCache(String cacheName, int cacheSize, ExampleDao exampleDao) throws Exception{
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }
    
    //Example of constructor that creates an in memory and disk cache
    public ExampleCache(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory, ExampleDao exampleDao) throws Exception {
        super(cacheName, cacheSize, diskPersist, dataDirectory);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid (return true), or whether it should be reloaded (return false).
    
        You can also just test for not null. ie: return (null != o).
        Returning true if not null, means the cached object is always valid and never has to be reloaded.

        If you return true here, your cached object will be returned in the 'get' call.
        If you return false here, your cached object will be reloaded using your 'loadData' method.
    */
    @Override
    public boolean isCacheItemValid(ExampleObjectToCache o) {
        //return o.isValid();        
        return (null != o);
    }

    /*
        You decide how to load the object you want to cache.
        This could be an api call, database call, etc.
    */
    @Override
    public ExampleObjectToCache loadData(String key) throws Exception {
        return this.exampleDao.get(key);
    }    
}
