package com.sg.simple.lru.cache;

/**
 *
 * @author sathayeg
 */
public interface DirLocate  {
    public String getPathToFile(String key) throws Exception;
}
