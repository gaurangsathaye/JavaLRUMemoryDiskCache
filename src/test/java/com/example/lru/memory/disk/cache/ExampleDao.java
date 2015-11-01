package com.example.lru.memory.disk.cache;

import java.util.Random;

/**
 *
 * @author sathayeg
 */
public class ExampleDao {
    public ExampleObjectToCache get(String key){
        //simulate time to load object
        //try{Thread.sleep( (new Random().nextInt(2) + 1) * 1000 );}catch(Exception e){}
        
        ExampleObjectToCache toCache = new ExampleObjectToCache(key);
        toCache.setData("The data for id: " + key);
        toCache.setLastModfied(System.currentTimeMillis());
        return toCache;
    }
}
