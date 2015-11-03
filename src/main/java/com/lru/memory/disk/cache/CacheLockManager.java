package com.lru.memory.disk.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 */
class CacheLockManager {
    private final Map<String,ReentrantReadWriteLock> cacheLockMap;
    private final int numberOfLocks;

    CacheLockManager(int numberOfLocks) {
        this.numberOfLocks=numberOfLocks;
        cacheLockMap = new HashMap<>();
        for(int i=0;i<this.numberOfLocks;i++) {
            cacheLockMap.put(Integer.toString(i), new ReentrantReadWriteLock(true));
        }
    }

    //hashcodes can be -ve,
    //so do abs on mod'ed value since the keys for the locks are all +ve integers
    ReentrantReadWriteLock getLock(int hashcode) {
        return cacheLockMap.get(Integer.toString(Math.abs(hashcode % numberOfLocks)));
    }
}