package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
interface DirLocate  {
    String getPathToFile(String key) throws Exception;
    boolean isDiskPersistent();
}
