package com.lru.memory.disk.cache;

/**
 *
 *  This is basic utility object you can use as a wrapper object for the real
 *  object you want to cache.
 * 
 * @author sathayeg
 * @param <T>
 */
public class CacheEntry<T> {
    private final T t;
    private final long timestamp;
    
    public CacheEntry(T t, long timestamp){
        this.t=t;
        this.timestamp=timestamp;
    }
    
    public T getCached(){
        return this.t;
    }
    
    public long getTimestamp(){
        return this.timestamp;
    }
}
