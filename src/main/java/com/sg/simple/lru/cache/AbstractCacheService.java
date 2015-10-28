package com.sg.simple.lru.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 * @param <T>
 */
public abstract class AbstractCacheService<T> implements DirLocate {
    
    private static final String KeyInternalGetFromDisk = "f";
    private static final String KeyInternalGetCachedObj = "o";

    protected LRUCache<String, T> cache;
    private final ReentrantReadWriteLock lock;

    private final AtomicLong statsMisses;
    private final AtomicLong statsHits;
    private final AtomicLong statsErr;
    private final String cacheName;
    private final int cacheSize;
    private final Map<String, Object> statsMap;
    private boolean persist;
    private String dataDir;

    public AbstractCacheService(String cacheName, int cacheSize) throws Exception {
        this(cacheName, cacheSize, false, null);
    }
    
    public AbstractCacheService(String cacheName, int cacheSize, boolean persistToFileSystem, String dataDirectory) throws Exception{
        if((null == cacheName) || cacheName.trim().equals("") || cacheSize < 1) throw new Exception("cacheName and/or cacheSize invalid");
        if(persistToFileSystem && ((null == dataDirectory) || dataDirectory.trim().equals("")) ) throw new Exception("Invalid data directory");
        this.cacheName = cacheName.trim();
        this.cache = new LRUCache<String, T>(cacheSize, dataDirectory);
        this.lock = new ReentrantReadWriteLock();
        this.statsHits = new AtomicLong(0L);
        this.statsMisses = new AtomicLong(0L);
        this.statsErr = new AtomicLong(0L);
        this.cacheSize = cacheSize;
        statsMap = new HashMap<String, Object>();
        this.persist = persistToFileSystem;
        this.dataDir = dataDirectory.trim().replaceAll("/$", "");        
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
        Map<String, Object> map = internalGetOnly(key);
        boolean fromDisk = (Boolean) map.get(KeyInternalGetFromDisk);
        T o = (T) map.get(KeyInternalGetCachedObj);
        if (isCacheItemValidInternal(o)) {
            statsHits.incrementAndGet();
            //Lazy load deserilized objects into memory
            if(fromDisk){
                p("From disk is true, lazy load, for key: " + key);
                internalPutOnly(key, o, false); //false because we don't want to re-serialize a deserialized object, this is to lazy load to memory
            }
            return o;
        }
        return putWithLookup(key);
    }

    public T putWithLookup(String key) throws Exception {
        lock.writeLock().lock();
        T o = ((T) internalGet(key).get(KeyInternalGetCachedObj));
        try {
            if (! isCacheItemValidInternal(o)) {
                o = loadData(key);
                if (null == o) {
                    throw new Exception("Key: " + key + " - Null values not allowed");
                }
                internalPut(key, o, true);
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
        internalPutOnly(key, o, true);
    }
    
    private void internalPutOnly(String key, T o, boolean overridePersist) throws Exception {
        if (null == o) {
            throw new Exception("Key: " + key + " - Null values not allowed");
        }
        lock.writeLock().lock();
        try {
            internalPut(key, o, overridePersist);
        } catch (Exception e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public T getOnly(String key) throws Exception {
        return ((T) internalGetOnly(key).get(KeyInternalGetCachedObj));
    }
    
    private Map<String, Object> internalGetOnly(String key) throws Exception {
        lock.readLock().lock();
        try {
            return internalGet(key);
        } catch (Exception e) {
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private Map<String, Object> internalGet(String key) throws Exception{  
        Map<String, Object> map = new HashMap<String, Object>();
        try{
            T t = cache.get(key);
            if(persist && (null == t)){
                t = deserialize(key);
                map.put(KeyInternalGetFromDisk, true);
                p("internalGet: post deserialize: key: " + key);
            }else{
                map.put(KeyInternalGetFromDisk, false);
            }
            p("internalGet: t is null: " + (null == t));
            map.put(KeyInternalGetCachedObj, t);
            return map;
        }catch(Exception e){
            throw e;
        }
    }
    
    private void internalPut(String key, T t, boolean overridePersist) throws Exception{        
        try{
            cache.put(key, t);
            if(persist && overridePersist){
                serialize(key, t);            
            }
        }catch(Exception e){
            throw e;
        }
    }
    
    private void serialize(String key, T t) throws LruCacheSerializationException{
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = new FileOutputStream(this.dataDir + "/" + key, false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(t);
        }catch(Exception e){
            throw new LruCacheSerializationException("Unable to serialize key: " + key, e);
        }finally{
            try{fos.close();}catch(Exception e){}
            try{oos.close();}catch(Exception e){}
        }
    }
    
    private T deserialize(String key) throws Exception{
        File f = new File(this.dataDir + "/" + key);
        if(! f.exists()) return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try{
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            return ( (T) ois.readObject() );
        }catch(Exception e){
            //Don't throw any errors here, delete the existing file
            //This may have been due to deserialization incompatibilities from cached item code changes, etc.
            try{
                f.delete();
            }catch(Exception exd){}
        }finally{
            try{fis.close();}catch(Exception e){}
            try{ois.close();}catch(Exception e){}
        }
        return null;
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
    
    static void p(Object o){
        //System.out.println(o);
    }

    @Override
    public String getDir(String parentDir, String key) throws Exception {
        if(Utl.areBlank(parentDir, key)) throw new Exception("parentDir: " + parentDir + ", key: " + key + " : invalid");
        return parentDir + "/" + key;
     }
    
} //end class
