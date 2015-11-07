package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
interface DiskOps  {
    String getPathToFile(String key) throws Exception;
    boolean isDiskPersistent();
}
