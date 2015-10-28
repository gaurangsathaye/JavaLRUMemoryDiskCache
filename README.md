# JavaLRUMemoryDiskCache

Java LRU Memory and Disk Cache

This is a thread safe, easy to use Java LRU in memory and disk cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object and loads it in the cache if not present. You instruct how object is loaded.  There are other methods like 'getOnly' and 'putOnly', but this is probably the only method you need.   
* Memory and disk storage: Cache is persisted to memory and disk. In case you restart your process, the cache will be lazy loaded from disk.
* `public final Map<String, Object> getStats()` - Get stats for your cache like hit ratio, cache size, hits, misses, etc.

**See the `com.example.lru.memory.disk.cache` package (in src/test) for an example and details on how to create and use the cache.)**  
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryOnly` : Example of how to use memory only cache.
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryAndDisk` : Example of how to use memory and disk cache.

## Create your cache:  

In your cache, override the two methods `isCacheItemValid` and `loadData`: (You do not call the `isCacheItemValid` and `loadData` methods directly.  You just need to define them, and they will be called by the internal cache as needed.  

In the constructor call,  
* `cacheName` is the name of your cache and is shown in the `getStats` call.  
* `cacheSize` is the total number of items your cache will store.  When you add more items in the the cache that are greater than `cacheSize`, older items are removed on an LRU (Least Recently Used) basis.  
* `dataDir` is a directory where cache items on disk are stored. (For memory and disk caching).  Each cache you create should have its own data directory.  This directory does not have to exist, however the process should have permissions to create it.  For first time usage, this directory should be empty.
* `true` in `super(cacheName, cacheSize, true, dataDir);` tells the cache that to use memory and disk caching

```java
public class ExampleCache1 extends AbstractCacheService<ExampleObjectToCache>{   
    private final ExampleDao exampleDao;
    
    //Example of constructor that creates an in memory cache only
    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao) throws Exception{
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }
    
    //Example of constructor that creates an in memory and disk cache
    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao, String dataDir) throws Exception {
        super(cacheName, cacheSize, true, dataDir);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid.
    
        You can also just test for not null. ie: return (null != o)

        If you return true here, your cached object will be returned in the 'get' call.
        If you return false here, your cached object will be reloaded using your 'loadData' method.
    */
    @Override
    public boolean isCacheItemValid(ExampleObjectToCache o) {
        //return o.isValid();        
        return (null != o);
    }

    /*
        You decide how to load the object you want to cache.
        This could be an api call, database call, etc.
    */
    @Override
    public ExampleObjectToCache loadData(String key) throws Exception {
        return this.exampleDao.get(key);
    }
    
}
```

## Use your cache:  
```java
public static ExampleCache1 cache = new ExampleCache1("ExampleCache1", 10000); //memory only
or
public static ExampleCache1 cache =  new ExampleCache1("ExampleCache1", 50000, new ExampleDao(), "/my/datadir/exampleCache1"); //memory and disk

ExampleMyObjectToCache myObject = cache.get("key");
Map<String, Object> stats = cache.getStats()
```

## Other points:  
* The cache key must be a string. 
* When using memory and disk caching, your cached objects must implement Serializable.  For memory only caching your cached objects do not have to implement Serializable.
* You cannot store null values in the cache. So if your `loadData(String key)` method returns a null object, the `public T get(String key)` call will throw an exception.  
* The `com.lru.memory.disk.cache.CacheEntry` object is a utility wrapper object you can store your real object in.  It has a default timestamp for when the object is created.  ie: `public class ExampleCache extends AbstractCacheService<CacheEntry<YourObjectToCache>>`

## Install (Maven)
* Download https://github.com/gaurangsathaye/JavaSimpleLRUCache/releases/download/1.0/JavaSimpleLRUCache-1.0.jar
* From the directory you downloaded the jar, run the following command to do a local maven install:  
  `mvn install:install-file -Dfile=JavaLRUMemoryDiskCache-1.0.jar -DgroupId=com.lru.memory.disk.cache -DartifactId=JavaLRUMemoryDiskCache -Dversion=1.0 -Dpackaging=jar`
* Add to `<dependencies>`  
```xml
<dependency>
    <groupId>com.lru.memory.disk.cache</groupId>
    <artifactId>JavaLRUMemoryDiskCache</artifactId>
    <version>1.0</version>
</dependency>
```

## To do:
* Asynch load cache item if invalid and return cached entry immediately.
