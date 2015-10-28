package com.sg.simple.lru.cache;

/**
 *
 * @author sathayeg
 */
public interface DirLocate  {
    public String getDir(String parentDir, String key) throws Exception;
}
