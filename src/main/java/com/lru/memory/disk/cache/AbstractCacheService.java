package com.lru.memory.disk.cache;

import static com.lru.memory.disk.cache.Utl.p;
import com.lru.memory.disk.cache.exceptions.BadRequestException;
import com.lru.memory.disk.cache.exceptions.InvalidCacheConfigurationException;
import com.lru.memory.disk.cache.exceptions.LoadDataIsNullException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 * @param <T>
 */
public abstract class AbstractCacheService<T> implements DiskOps {

    private static final String KeyInternalGetFromDisk = "f";
    private static final String KeyInternalGetCachedObj = "o";
    private static final int NumberDiskShards = 1000;
    private static final int ConcurrencyLevel = 200;

    private LRUCache<String, T> cache;
    private final ReentrantReadWriteLock lock;

    private final AtomicLong statsMisses;
    private final AtomicLong statsHits;
    private final AtomicLong statsHitsDisk;
    private final AtomicLong statsHitsMemory;
    private final AtomicLong statsRemoteSuccesses;
    private final AtomicLong statsRemoteNetworkErrs;
    private final AtomicLong statsRemoteSevereErrs;
    private final String cacheName;
    private final int cacheSize;
    private final Map<String, Object> statsMap;
    private boolean persist;
    private String dataDir;
    private CacheLockManager lockMgr;
    private DistributedManager distMgr;
    private boolean distributed;

    public AbstractCacheService(String cacheName, int cacheSize) throws Exception {
        this(cacheName, cacheSize, false, null);
    }

    public AbstractCacheService(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory) throws InvalidCacheConfigurationException, Exception {
        if (Utl.areBlank(cacheName) || cacheSize < 1) {
            throw new InvalidCacheConfigurationException("cacheName and/or cacheSize invalid", null);
        }
        if (diskPersist && Utl.areBlank(dataDirectory)) {
            throw new InvalidCacheConfigurationException("Invalid data directory", null);
        }
        this.cacheName = cacheName.trim();
        this.lock = new ReentrantReadWriteLock();
        this.statsHits = new AtomicLong(0L);
        this.statsHitsDisk = new AtomicLong(0L);
        this.statsHitsMemory = new AtomicLong(0L);
        this.statsMisses = new AtomicLong(0L);
        this.statsRemoteSuccesses = new AtomicLong(0L);
        this.statsRemoteNetworkErrs = new AtomicLong(0L);
        this.statsRemoteSevereErrs = new AtomicLong(0L);
        this.cacheSize = cacheSize;
        this.statsMap = new HashMap<>();
        this.persist = diskPersist;
        this.cache = new LRUCache<>(cacheSize, this);
        this.lockMgr = new CacheLockManager(ConcurrencyLevel);
        if (this.persist) {
            this.dataDir = dataDirectory.trim().replaceAll("/$", "");
            init();
        }
    }

    public abstract boolean isCacheItemValid(T o);

    public abstract T loadData(String key) throws Exception;

    public String getCacheName() {
        return this.cacheName;
    }

    public T get(String key) throws Exception {//throws Exception {
        if (Utl.areBlank(key)) {
            throw new BadRequestException("key is blank", null);
        }

        if (this.distributed && (null != this.distMgr)) {
            T cachedObj = getDistributed(key);
            if(null != cachedObj){
                p("remote-000");
                return cachedObj;
            }else{
                p("remote-null-000");
            }
        }
        
        p("local-000");
        return getNonDistributed(key);
    }

    //Returns null or cached object or throws exceptions
    T getDistributed(String key) throws LoadDataIsNullException, BadRequestException {      
        DistributedConfigServer clusterServerForCacheKey = null;
        try {
            if (Utl.areBlank(key)) {
                p("getDistributed bad request return null");
                throw new BadRequestException("key is blank", null);
            }

            clusterServerForCacheKey = this.distMgr.getClusterServerForCacheKey(key);
            p("distributed request info: server for key: " + key + 
                    ", cluster server: "+ clusterServerForCacheKey.toString());

            if (clusterServerForCacheKey.isSelf() || (! clusterServerForCacheKey.tryRemote())) {
                p("getDistributed return null:"+clusterServerForCacheKey.isSelf() + ":"+clusterServerForCacheKey.tryRemote());
                return null;
            }

            DistributedRequestResponse<Serializable> distrr = DistributedClient.distributedCacheGet(cacheName, key, clusterServerForCacheKey);
            p("distrr for key: " + key + " :: " + distrr.toString());

            if (distrr.getServerSetErrorLevel() >= DistributedServer.ServerErrorLevelSevere) {
                this.statsRemoteSevereErrs.incrementAndGet();
                clusterServerForCacheKey.setSevereErrorNextAttemptTimestamp();
                p("getDistributed severe err return null");
                return null;
            }

            Serializable serverSetData = distrr.getServerSetData();
            if (null != serverSetData) {
                try {
                    this.statsRemoteSuccesses.incrementAndGet();
                    return ((T) serverSetData);
                } catch (Exception e) {
                    p("error: Unable to cast remote data for key: " + key + " :: " + e);
                    this.statsRemoteSevereErrs.incrementAndGet();
                    clusterServerForCacheKey.setSevereErrorNextAttemptTimestamp();
                    p("getDistributed cast exception return null");
                    return null;
                }
            } else {
                this.statsRemoteSuccesses.incrementAndGet();
                p("getDistributed serverSetData return null");
                throw new LoadDataIsNullException("Distributed get value is null for key: " + key, null);
            }

        } catch (BadRequestException e) {
            p("error: AbstractCacheService.getDistributed: key: " + key + " :: " + e);
            throw e;
        } catch (SocketException e) {
            p("error: AbstractCacheService.getDistributed: key: " + key + " :: " + e);
            this.statsRemoteNetworkErrs.incrementAndGet();
            clusterServerForCacheKey.setNetworkErrorNextAttemptTimestamp();
        } catch (IOException e) {
            p("error: AbstractCacheService.getDistributed: key: " + key + " :: " + e);
            this.statsRemoteNetworkErrs.incrementAndGet();
            clusterServerForCacheKey.setNetworkErrorNextAttemptTimestamp();
        } catch (ClassNotFoundException e) {
            p("error: AbstractCacheService.getDistributed: key: " + key + " :: " + e);
            this.statsRemoteSevereErrs.incrementAndGet();
            clusterServerForCacheKey.setSevereErrorNextAttemptTimestamp();
        }
        p("getDistributed return last null");
        return null;
    }

    T getNonDistributed(String key) throws Exception {
        if (Utl.areBlank(key)) {
            throw new Exception("key is blank");
        }

        Map<String, Object> map = internalGetOnly(key, true);
        boolean fromDisk = (Boolean) map.get(KeyInternalGetFromDisk);
        T o = (T) map.get(KeyInternalGetCachedObj);
        if (isCacheItemValidInternal(o)) {
            //Lazy load disk objects into memory
            if (fromDisk) {
                internalPutOnly(key, o, false); //false because we don't want to re-serialize a deserialized object, this is to lazy load to memory
            }
            return o;
        }
        return internalPutWithLookup(key);
    }

    public T putWithLookup(String key) throws Exception {
        if (this.distributed) {
            throw new Exception("This method is not allowed in distributed configuration");
        }

        return internalPutWithLookup(key);
    }

    public void putOnly(String key, T o) throws Exception {
        if (this.distributed) {
            throw new Exception("This method is not allowed in distributed configuration");
        }

        internalPutOnly(key, o, true);
    }

    public T getOnly(String key) throws Exception {
        if (this.distributed) {
            throw new Exception("This method is not allowed in distributed configuration");
        }

        return ((T) internalGetOnly(key, true).get(KeyInternalGetCachedObj));
    }

    public final Map<String, Object> getStats() {
        long hits = statsHits.get();
        long misses = statsMisses.get();
        statsMap.put("cacheName", cacheName);
        statsMap.put("hits", hits);
        statsMap.put("hitsDisk", statsHitsDisk.get());
        statsMap.put("hitsMemory", statsHitsMemory.get());
        statsMap.put("remoteSuccesses", this.statsRemoteSuccesses.get());
        statsMap.put("remoteErrNetwork", this.statsRemoteNetworkErrs.get());
        statsMap.put("remoteErrSevere", this.statsRemoteSevereErrs.get());
        statsMap.put("misses", misses);
        statsMap.put("hitratio",
                (hits < 1) ? 0.0 : (((double) hits) / ((double) (hits + misses)))
        );
        statsMap.put("cacheMaxSize", cacheSize);
        statsMap.put("cacheCurrentSize", cache.size());
        if(this.persist) statsMap.put("dataDir", this.dataDir);
        return statsMap;
    }

    public void clear() throws Exception {
        lock.writeLock().lock();
        try {
            cache.clear();
        } catch (Exception e) {
            throw e;
        } finally {
            lock.writeLock().unlock();
        }
    }

    //Start DiskOps impl
    @Override
    public String getPathToFile(String key) throws Exception {
        if (!isDiskPersistent()) {
            return null;
        }
        if (Utl.areBlank(key)) {
            throw new Exception("key: " + key + " : invalid");
        }
        return this.dataDir + "/" + (Math.abs(key.hashCode()) % NumberDiskShards) + "/" + Utl.sha256(key);
    }

    @Override
    public boolean isDiskPersistent() {
        return this.persist;
    }
    //End DiskOps impl

    private T internalPutWithLookup(String key) throws Exception {
        ReentrantReadWriteLock.WriteLock ldWriteLock = this.lockMgr.getLock(key.hashCode()).writeLock();
        ldWriteLock.lock();
        T o = ((T) internalGetOnly(key, false).get(KeyInternalGetCachedObj));
        try {
            if (!isCacheItemValidInternal(o)) {
                o = loadData(key);
                if (null == o) {
                    throw new LoadDataIsNullException("Key: " + key + " - Null values not allowed", null);
                }
                internalPutOnly(key, o, true);
            }
            return o;
        } catch (Exception e) {
            throw e;
        } finally {
            ldWriteLock.unlock();
        }
    }

    private boolean isCacheItemValidInternal(T o) {
        try {
            return isCacheItemValid(o);
        } catch (Exception e) {
            return false;
        }
    }

    private void init() throws Exception {
        File dir = new File(this.dataDir);
        if (dir.exists() && dir.isFile()) {
            throw new Exception("data dir: " + this.dataDir + ", is a file, should be a directory");
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < NumberDiskShards; i++) {
            File shardDir = new File(dir, Integer.toString(i));
            if (shardDir.exists() && shardDir.isDirectory()) {
                continue;
            }
            if (shardDir.exists() && (!shardDir.isDirectory())) {
                shardDir.delete();
            }
            shardDir.mkdir();
        }
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

    private Map<String, Object> internalGetOnly(String key, boolean doStats) throws Exception {
        lock.readLock().lock();
        try {
            Map<String, Object> map = internalGet(key);
            if (doStats) {
                boolean tryDisk = (Boolean) map.get(KeyInternalGetFromDisk);
                T t = (T) map.get(KeyInternalGetCachedObj);
                if (null != t) {
                    statsHits.incrementAndGet();
                    if (tryDisk) {
                        statsHitsDisk.incrementAndGet();
                    } else {
                        statsHitsMemory.incrementAndGet();
                    }
                } else {
                    statsMisses.incrementAndGet();
                }
            }
            return map;
        } catch (Exception e) {
            throw e;
        } finally {
            lock.readLock().unlock();
        }
    }

    private Map<String, Object> internalGet(String key) throws Exception {
        Map<String, Object> map = new HashMap<>();
        try {
            T t = cache.get(key);
            if (persist && (null == t)) {
                try{
                    t = (T) (Utl.deserializeFile(this.getPathToFile(key)));
                }catch(Exception e){}
                map.put(KeyInternalGetFromDisk, true);
            } else {
                map.put(KeyInternalGetFromDisk, false);
            }
            map.put(KeyInternalGetCachedObj, t);
            return map;
        } catch (Exception e) {
            throw e;
        }
    }

    private void internalPut(String key, T t, boolean overridePersist) throws Exception {
        try {
            cache.put(key, t);
            if (persist && overridePersist) {
                //serialize(key, t);
                Utl.offerToGlobalExecutorService(new AsyncFileSerialize(this.getPathToFile(key), (Serializable) t));
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /*private void serialize(String key, T t) throws LruCacheSerializationException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(this.getPathToFile(key), false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(t);
        } catch (Exception e) {
            throw new LruCacheSerializationException("Unable to serialize key: " + key, e);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
            try {
                oos.close();
            } catch (Exception e) {
            }
        }
    }*/
    
    /*
    private T deserialize(String key) throws Exception {
        File f = new File(this.getPathToFile(key));
        if (!f.exists()) {
            return null;
        }
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            return ((T) ois.readObject());
        } catch (Exception e) {
            //Don't throw any errors here, delete the existing file
            //This may have been due to deserialization incompatibilities from serial version uid or cached item code changes, etc.
            try {
                f.delete();
            } catch (Exception exd) {
            }
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
            try {
                ois.close();
            } catch (Exception e) {
            }
        }
        return null;
    }*/

    //Distributed
    void setDistributedManager(DistributedManager dm) throws Exception {
        if (null == dm) {
            throw new Exception("Distributed Manager is null");
        }
        this.distMgr = dm;
        this.distributed = true;
    }
}
