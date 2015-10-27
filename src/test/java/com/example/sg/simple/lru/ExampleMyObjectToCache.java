package com.example.sg.simple.lru;

/**
 *
 * @author sathayeg
 */
public class ExampleMyObjectToCache {
    private long lastModfied = 0;
    private String data;
    private final String id;
    
    public ExampleMyObjectToCache(String id){
        this.id = id;
    }
    
    /*
        You choose how to determine if your object is valid in the cache.
    
        Here as an example, we are saying that if last modified is less than
        20 seconds from the current time, it is valid, otherwise it is not.
    */
    public boolean isValid(){
        return ((System.currentTimeMillis() - lastModfied) < 20000);
    }

    public long getLastModfied() {
        return lastModfied;
    }

    public void setLastModfied(long lastModfied) {
        this.lastModfied = lastModfied;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ExampleMyObjectToCache{" + "lastModfied=" + lastModfied + ", data=" + data + ", id=" + id + '}';
    }
    
}
