package com.lru.memory.disk.cache;

import java.io.Serializable;

/**
 *
 * @author sathayeg
 */
public interface DiskOps  {
    public String getPathToFile(String key) throws Exception;
    public boolean isDiskPersistent();
    public void asyncSerialize(String key, Serializable obj);
    public void asyncDelete(String key);
}
