# Java LRU Memory Disk Cache

This is a thread safe, easy to use Java LRU in memory and disk cache.  

Some of the benefits of using the cache are...  
* **`public T get(String key)`** - Gets your object from the cache.  If your object is not in cache, it is loaded and put into the cache. Loading the object and putting it into the cache, are all done for you behind the scenes. You tell the cache how to load your objects (see below).  There are other methods like `getOnly` and `putOnly` but this is probably the only method you need.
* **Memory and disk**: Cached objects are stored in memory and persisted on disk/file system (optional). In case you restart your process, the in memory cache will be lazy loaded from disk.  You don't lose your cached data after stopping and starting your app.
* **Concurrency and thread safety**: Cache can be used in high request load and multi thread environments.  Reads are fully concurrent, writes have a high level of concurrency due to multi stage write locks.
* **`public final Map<String, Object> getStats()`** - Get stats for your cache like hit ratio, cache size, hits, misses, etc.

**See the `com.example.lru.memory.disk.cache` package (in src/test) for an example and details on how to create and use the cache.)**  
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryOnly` : Example of how to use memory only cache.
* `com.example.lru.memory.disk.cache.ExampleUsageMemoryAndDisk` : Example of how to use memory and disk cache.
* `com.example.lru.memory.disk.cache.ExampleCache` : Example memory and disk, and memory only cache.
* 
* `com.example.lru.memory.disk.cache.ExampleUsageCacheEntryCache` and `ExampleCacheEntryCache` : Example of how to use memory and disk cache with `CacheEntry` wrapper object.

## Create your cache:  

Override the two methods `isCacheItemValid` and `loadData`: (You do not call the `isCacheItemValid` and `loadData` methods directly.  You just need to define them, and they will be called by the internal cache as needed.  

**Cache constructor call**:    
* `String` **`cacheName`** is the name of your cache and is shown in the `getStats` call.  
* `int` **`cacheSize`** is the total number of items your cache will store.  When you add more items in the the cache that are greater than `cacheSize`, older items are removed on an LRU (Least Recently Used) basis.  
* `boolean` **`diskPersist`** (only for memory AND disk cache) tells the cache to use memory AND disk caching
* `String` **`dataDirectory`** (only for memory AND disk cache) is the directory where cache items are stored on disk.  Each cache you create should have its own unique data directory.  This directory does not have to exist, however the process should have permissions to create it.  For first time usage, this directory should be empty.
* Since you are creating the cache, you can pass in any additional params to your constructor.  In the `com.example.lru.memory.disk.cache.ExampleCache` class, we pass in the `ExampleDao`.

```java
public class ExampleCache extends AbstractCacheService<ExampleObjectToCache>{   
    private final ExampleDao exampleDao;
    
    //Example of constructor that creates an in memory cache only
    public ExampleCache(String cacheName, int cacheSize, ExampleDao exampleDao) throws Exception{
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }
    
    //Example of constructor that creates an in memory and disk cache
    public ExampleCache(String cacheName, int cacheSize, boolean diskPersist, String dataDirectory, ExampleDao exampleDao) throws Exception {
        super(cacheName, cacheSize, diskPersist, dataDirectory);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid (return true), or whether it should be reloaded (return false).
    
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
public static ExampleCache cacheMemoryOnly = new ExampleCache("ExampleCacheMem", 10000, new ExampleDao()); //memory only
or
public static ExampleCache cacheMemoryAndDisk =  new ExampleCache("ExampleCacheMemDisk", 10, true, "/data/directory/forMyObject", new ExampleDao()); //memory and disk

ExampleMyObjectToCache myObject = cacheMemoryAndDisk.get("key");
Map<String, Object> stats = cacheMemoryAndDisk.getStats()
```
You can create as many caches as you need. However you should use a single shareable instance of each cache you create.  Caches are thread safe and the same instance of each cache should be used throughout your application.  
**For example:**  
```java
public class CarsCache extends AbstractCacheService<Car>{..isCacheItemValid(Car c){..}..Car loadData(String key)..}
public static CarsCache carsMemDiskCache = new CarsCache("CarsCache", 50000, true, "/data/directory/cars", new CarDao());
Car porsche = carsMemDiskCache.get("911");

public class JsonCache extends AbstractCacheService<String>{..isCacheItemValid(String s){..}..String loadData(String key)..}
public static JsonCache jsonMemDiskCache = new JsonCache("JsonCache", 50000, true, "/data/directory/json", new JsonLoader());
String json = jsonMemDiskCache.get("http://jsonurl.com/file.html");

public class BlogsCache extends AbstractCacheService<Blog>{..isCacheItemValid(Blog b){..}..Blog loadData(String key)..}
public static BlogsCache blogsMemOnlyCache = new BlogsCache("BlogsCache", 50000, new BlogsDao());
Blog techBlog = blogsMemOnlyCache.get("blogID");
```

## Other points:  
* **The cache key must be a string (java.lang.String).**
* **When using memory and disk caching, your cached objects must implement Serializable.  For memory only caching, your cached objects do not have to implement Serializable.**
* **When using the `CacheEntry` wrapper object, your cached objects must implement Serializable**
* You cannot store null values in the cache. So if your `loadData(String key)` method returns a null object, the `public T get(String key)` call will throw an exception.  If you want to store 'NULL' objects, consider creating a cache as follows: `ExampleCache extends AbstractCacheService<CacheEntry<YourObjectToCache>>`, where the object you store in `CacheEntry` is NULL.
* The `com.lru.memory.disk.cache.CacheEntry` object is a utility wrapper object you can store your real object in.  It has a constructor `public CacheEntry(T t, long timestamp)`.  `timestamp` can, for example, be used in your `isCacheItemValid` implementation.  (See the `com.example.lru.memory.disk.cache.ExampleUsageCacheEntryCache` example).  The `CacheEntry` object has `public T getCached()` and `public long getTimestamp()` methods. `getCached()` returns your object and `getTimestamp()` returns the timestamp used in the constructor.
* When the number of items in the cache become greater than `cacheSize` (see above), and cached objects fall out of memory via the LRU, they will also be removed from disk (If using disk caching).  The disk cache will contain as many and possibly more items than are present in memory.  This will be evident when the cache (memory and disk) is used over your application's stop start cycles.

## Install (Maven)
* Download https://github.com/gaurangsathaye/JavaLRUMemoryDiskCache/releases/download/1.1/JavaLRUMemoryDiskCache-1.1.jar
* From the directory you downloaded the jar, run the following command to do a local maven install:  
  `mvn install:install-file -Dfile=JavaLRUMemoryDiskCache-1.1.jar -DgroupId=com.lru.memory.disk.cache -DartifactId=JavaLRUMemoryDiskCache -Dversion=1.1 -Dpackaging=jar`
* Add to your project's `<dependencies>`  
```xml
<dependency>
    <groupId>com.lru.memory.disk.cache</groupId>
    <artifactId>JavaLRUMemoryDiskCache</artifactId>
    <version>1.1</version>
</dependency>
```

## To do:
* Distributed caching
