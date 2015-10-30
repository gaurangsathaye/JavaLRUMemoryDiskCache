# JavaLRUMemoryDiskCache

Java LRU Memory and Disk Cache

This is a thread safe, easy to use Java LRU in memory and disk cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object from the cache.  If object is not in cache, it is loaded and put into the cache. Loading the object and putting into cache are all done for you behind the scenes. You tell the cache how to load your objects (see below).  There are other methods like `getOnly` and `putOnly` but this is probably the only method you need.
* **Memory and disk storage**: Cached objects are stored in memory and disk / file system (optional). In case you restart your process, the in memory cache will be lazy loaded from disk.  You don't lose your cache when starting and stopping your app.
* `public final Map<String, Object> getStats()` - Get stats for your cache like hit ratio, cache size, hits, misses, etc.

**See the `com.example.lru.memory.disk.cache` package (in src/test) for an example and details on how to create and use the cache.)**  
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryOnly` : Example of how to use memory only cache.
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryAndDisk` : Example of how to use memory and disk cache.

## Create your cache:  

Override the two methods `isCacheItemValid` and `loadData`: (You do not call the `isCacheItemValid` and `loadData` methods directly.  You just need to define them, and they will be called by the internal cache as needed.  

In ExampleCache1 constructor call,  
* `cacheName` is the name of your cache and is shown in the `getStats` call.  
* `cacheSize` is the total number of items your cache will store.  When you add more items in the the cache that are greater than `cacheSize`, older items are removed on an LRU (Least Recently Used) basis.  
* `diskPersist` (only for memory AND disk cache) tells the cache to use memory AND disk caching
* `dataDirectory` (only for memory AND disk cache) is the directory where cache items are stored on disk.  Each cache you create should have its own unique data directory.  This directory does not have to exist, however the process should have permissions to create it.  For first time usage, this directory should be empty.
* Since you are creating the cache, you can pass in any additional params to your constructor.  In this example, we pass in the `ExampleDao`.

```java
public class ExampleCache1 extends AbstractCacheService<ExampleObjectToCache>{   
    private final ExampleDao exampleDao;
    
    //Example of constructor that creates an in memory cache only
    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao) throws Exception{
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }
    
    //Example of constructor that creates an in memory and disk cache
    public ExampleCache1(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory, ExampleDao exampleDao) throws Exception {
        super(cacheName, cacheSize, true, dataDirectory);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid.
    
        You can also just test for not null. ie: return (null != o).
        Returning true if not null, means the cached object is always valid and never has to be reloaded.

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
public static ExampleCache1 cacheMemoryOnly = new ExampleCache1("ExampleCache1", 10000, new ExampleDao()); //memory only
or
public static ExampleCache1 cacheMemoryAndDisk =  new ExampleCache1("ExampleCache1", 10, true, "/data/directory/forMyObject", new ExampleDao()); //memory and disk

ExampleMyObjectToCache myObject = cache.get("key");
Map<String, Object> stats = cache.getStats()
```

## Other points:  
* **The cache key must be a string (java.lang.String).**
* **When using memory and disk caching, your cached objects must implement Serializable.  For memory only caching, your cached objects do not have to implement Serializable.**
* You cannot store null values in the cache. So if your `loadData(String key)` method returns a null object, the `public T get(String key)` call will throw an exception.  
* The `com.lru.memory.disk.cache.CacheEntry` object is a utility wrapper object you can store your real object in.  It has a default timestamp for when the `CacheEntry` object is created.  ie: `public class ExampleCache extends AbstractCacheService<CacheEntry<YourObjectToCache>>`
* When the number of items in the cache become greater than `cacheSize` (see above), and cached objects fall out memory via the LRU, they will also be removed from disk (If using disk caching).  The disk cache will contain as many and possibly more items than are present in memory.  This will be evident when the cache (memory and disk) is used over your application's stop start cycles.

## Install (Maven)
* Download https://github.com/gaurangsathaye/JavaLRUMemoryDiskCache/releases/download/1.0/JavaLRUMemoryDiskCache-1.0.jar
* From the directory you downloaded the jar, run the following command to do a local maven install:  
  `mvn install:install-file -Dfile=JavaLRUMemoryDiskCache-1.0.jar -DgroupId=com.lru.memory.disk.cache -DartifactId=JavaLRUMemoryDiskCache -Dversion=1.0 -Dpackaging=jar`
* Add to your project's `<dependencies>`  
```xml
<dependency>
    <groupId>com.lru.memory.disk.cache</groupId>
    <artifactId>JavaLRUMemoryDiskCache</artifactId>
    <version>1.0</version>
</dependency>
```

## To do:
* Asynch load cache item if invalid and return cached entry immediately.
