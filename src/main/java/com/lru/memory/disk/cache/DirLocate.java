package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
public interface DirLocate  {
    public String getPathToFile(String key) throws Exception;
    public boolean isDiskPersistent();
}
