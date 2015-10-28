package com.example.sg.simple.lru;

import com.sg.simple.lru.cache.AbstractCacheService;

/**
 *
 * @author sathayeg
 */
public class ExampleCache1 extends AbstractCacheService<ExampleObjectToCache>{   
    private final ExampleDao exampleDao;
    
    //Example of constructor that creates an in memory cache only
    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao) throws Exception{
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }
    
    //Example of constructor that creates an in memory and disk cache
    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao, boolean persist, String dataDir) throws Exception {
        super(cacheName, cacheSize, persist, dataDir);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid.
    
        You can also just test for not null. ie: return (null != o)

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
