package com.sg.simple.lru.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 * @param <T>
 */
public abstract class AbstractCacheService<T> {

    protected LRUCache<String, T> cache;
    private final ReentrantReadWriteLock lock;

    private final AtomicLong statsMisses;
    private final AtomicLong statsHits;
    private final AtomicLong statsErr;
    private final String cacheName;
    private final int cacheSize;
    private final Map<String, Object> statsMap;

    public AbstractCacheService(String cacheName, int cacheSize) {
        this.cacheName = cacheName;
        this.cache = new LRUCache<String, T>(cacheSize);
        this.lock = new ReentrantReadWriteLock();
        this.statsHits = new AtomicLong(0L);
        this.statsMisses = new AtomicLong(0L);
        this.statsErr = new AtomicLong(0L);
        this.cacheSize = cacheSize;
        statsMap = new HashMap<String, Object>();
    }

    public abstract boolean isCacheItemValid(T o);
    
    private boolean isCacheItemValidInternal(T o) {
        try{
            return isCacheItemValid(o);
        }catch(Exception e){
            return false;
        }
    }

    public abstract T loadData(String key) throws Exception;

    public T get(String key) throws Exception {
        T o = getOnly(key);
        if (isCacheItemValidInternal(o)) {
            statsHits.incrementAndGet();
            return o;
        }
        return putWithLookup(key);
    }

    public T putWithLookup(String key) throws Exception {
        lock.writeLock().lock();
        T o = cache.get(key);
        try {
            if (! isCacheItemValidInternal(o)) {
                o = loadData(key);
                if (null == o) {
                    throw new Exception("Key: " + key + " - Null values not allowed");
                }
                cache.put(key, o);
                statsMisses.incrementAndGet();
            } else {
                statsHits.incrementAndGet();
            }
            return o;
        } catch (Exception e) {
            statsErr.incrementAndGet();
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void putOnly(String key, T o) throws Exception {
        if (null == o) {
            throw new Exception("Key: " + key + " - Null values not allowed");
        }
        lock.writeLock().lock();
        try {
            cache.put(key, o);
        } catch (Exception e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T getOnly(String key) throws Exception {
        lock.readLock().lock();
        try {
            return cache.get(key);
        } catch (Exception e) {
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }

    public final Map<String, Object> getStats() {
        long hits = statsHits.get();
        long misses = statsMisses.get();
        statsMap.put("cacheName", cacheName);
        statsMap.put("hits", hits);
        statsMap.put("misses", misses);
        statsMap.put("err", statsErr.get());
        statsMap.put("hitratio",
                (hits < 1) ? 0.0 : (((double) hits) / ((double) (hits + misses)))
        );
        statsMap.put("cacheMaxSize", cacheSize);
        statsMap.put("cacheCurrentSize", cache.size());
        return statsMap;
    }

} //end class
