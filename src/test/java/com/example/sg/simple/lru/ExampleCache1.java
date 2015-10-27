package com.example.sg.simple.lru;

import com.sg.simple.lru.cache.AbstractCacheService;

/**
 *
 * @author sathayeg
 */
public class ExampleCache1 extends AbstractCacheService<ExampleMyObjectToCache>{

    public ExampleCache1(String cacheName, int cacheSize) {
        super(cacheName, cacheSize);
    }

    @Override
    public boolean isCacheItemValid(ExampleMyObjectToCache o) {
        return o.isValid();
    }

    /*
        You decide how to load the object you want to cache.
        This could be an api call, database call, etc.
    */
    @Override
    public ExampleMyObjectToCache loadData(String key) throws Exception {
        ExampleMyObjectToCache toCache = new ExampleMyObjectToCache(key);
        toCache.setData("The data for id: " + key);
        toCache.setLastModfied(System.currentTimeMillis());
        return toCache;
    }
    
}
