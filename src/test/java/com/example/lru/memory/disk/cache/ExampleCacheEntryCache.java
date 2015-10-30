package com.example.lru.memory.disk.cache;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.CacheEntry;

/**
 *
 * @author sathayeg
 */
public class ExampleCacheEntryCache extends AbstractCacheService<CacheEntry<ExampleObjectToCache>> {

    public ExampleCacheEntryCache(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory) throws Exception {
        super(cacheName, cacheSize, diskPersist, dataDirectory);
    }

    @Override
    public boolean isCacheItemValid(CacheEntry<ExampleObjectToCache> o) {
        //item only valid for 5 seconds, after which it will be reloaded
        return ((System.currentTimeMillis() - o.getTimestamp()) < 5000);
    }

    @Override
    public CacheEntry<ExampleObjectToCache> loadData(String key) throws Exception {
        CacheEntry<ExampleObjectToCache> ce = null;
        if(key.equals("nullObject")){
             ce = new CacheEntry<>(null, System.currentTimeMillis());
        }else{
            ExampleObjectToCache obj = new ExampleObjectToCache(key);
            obj.setData("data for key: " + key);
            obj.setLastModfied(System.currentTimeMillis());
            ce = new CacheEntry<>(obj, System.currentTimeMillis());
        }
        return ce;
    }

   
    
}
