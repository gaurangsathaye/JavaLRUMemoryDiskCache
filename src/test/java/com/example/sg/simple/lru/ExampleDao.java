package com.example.sg.simple.lru;

/**
 *
 * @author sathayeg
 */
public class ExampleDao {
    public ExampleMyObjectToCache get(String key){
        ExampleMyObjectToCache toCache = new ExampleMyObjectToCache(key);
        toCache.setData("The data for id: " + key);
        toCache.setLastModfied(System.currentTimeMillis());
        return toCache;
    }
}
