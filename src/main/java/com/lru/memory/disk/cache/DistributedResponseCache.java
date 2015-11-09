package com.lru.memory.disk.cache;

import java.io.Serializable;

/**
 *
 * @author sathayeg
 */
public class DistributedResponseCache extends AbstractCacheService<CacheEntry<Serializable>>{

    public DistributedResponseCache(String cacheName, int cacheSize) throws Exception {
        super(cacheName, cacheSize);
    }

    @Override
    public boolean isCacheItemValid(CacheEntry<Serializable> o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CacheEntry<Serializable> loadData(String key) throws Exception {
        throw new UnsupportedOperationException();
    }
    
}
