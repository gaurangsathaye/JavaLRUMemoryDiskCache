package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
public class ServerCache extends AbstractCacheService<CacheEntry<String>> {

    public ServerCache(String cacheName, int cacheSize) throws Exception {
        super(cacheName, cacheSize);
    }
    
    public ServerCache(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory) throws Exception {
        super(cacheName, cacheSize, diskPersist, dataDirectory);
    }

    @Override
    public CacheEntry<String> get(String key) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }    

    @Override
    public boolean isCacheItemValid(CacheEntry<String> o) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public CacheEntry<String> loadData(String key) throws Exception {
        throw new UnsupportedOperationException("Not supported.");
    }
    
}
