package com.example.sg.simple.lru;

/**
 *
 * @author sathayeg
 */
public class ExampleDao {
    public ExampleObjectToCache get(String key){
        ExampleObjectToCache toCache = new ExampleObjectToCache(key);
        toCache.setData("The data for id: " + key);
        toCache.setLastModfied(System.currentTimeMillis());
        return toCache;
    }
}
