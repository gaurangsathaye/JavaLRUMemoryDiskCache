# JavaSimpleLRUCache

Java Simple LRU Cache

This is a thread safe, easy to use Java LRU in memory cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object and loads it in the cache if not present. You instruct how object is loaded.  There are other methods like 'getOnly' and 'putOnly', but this is probably the only method you need.   
* `public final Map<String, Object> getStats()` - Get stats for your cache like hit ratio, cache size, hits, misses, etc.

**See the `com.example.sg.simple.lru` package (in src/test) for an example and details on how to create and use the cache. Start with `Example1.java`)**

## Create your cache:  

In your cache, override the two methods `isCacheItemValid` and `loadData`: (You do not call the `isCacheItemValid` and `loadData` methods directly.  You just need to define them, and they will be called by the internal cache as needed.  

In the constructor call, `cacheName` is the name of your cache and is shown in the `getStats` call.  `cacheSize` is the total number of items your cache will store.  When you add more items in the the cache that are greater than `cacheSize`, older items are removed on an LRU (Least Recently Used) basis.  
```java
public class ExampleCache1 extends AbstractCacheService<ExampleMyObjectToCache>{
    private final ExampleDao exampleDao;

    public ExampleCache1(String cacheName, int cacheSize, ExampleDao exampleDao) {
        super(cacheName, cacheSize);
        this.exampleDao = exampleDao;
    }

    /*
        You decide if your cached object is valid.

        You can use timestamps, last modified or any other parameters to determine
        if your cached object is valid.

        If you return true here, your cached object will be returned in the 'get' call.
        If you return false here, your cached object will be reloaded using your 'loadData' method.
    */
    @Override
    public boolean isCacheItemValid(ExampleMyObjectToCache o) {
        return o.isValid();
    }

    /*
        You decide how to load the object you want to cache.
        This could be an api call, database call, etc.
    */
    @Override
    public ExampleMyObjectToCache loadData(String key) throws Exception {
        return this.exampleDao.get(key);
    }
    
}
```

## Use your cache:  
```java
public static ExampleCache1 cache = new ExampleCache1("ExampleCache1", 10000);
ExampleMyObjectToCache myObject = cache.get("key");
Map<String, Object> stats = cache.getStats()
```

## Other points:  
* The cache key must be a string. 
* You cannot store null values in the cache. So if your `loadData(String key)` method returns a null object, the `public T get(String key)` call will throw an exception.  
* The `com.sg.simple.lru.cache.CacheEntry` object is a utility wrapper object you can store your real object in.  It has a default timestamp for when the object is created.  ie: `public class ExampleCache extends AbstractCacheService<CacheEntry<YourObjectToCache>>`

## Install (Maven)
* Download https://github.com/gaurangsathaye/JavaSimpleLRUCache/releases/download/1.0/JavaSimpleLRUCache-1.0.jar
* From the directory you downloaded the jar, run the following command to do a local maven install:  
  `mvn install:install-file -Dfile=JavaSimpleLRUCache-1.0.jar -DgroupId=com.sg.simple.lru.cache -DartifactId=JavaSimpleLRUCache -Dversion=1.0 -Dpackaging=jar`
* Add to `<dependencies>`  
```xml
<dependency>
    <groupId>com.sg.simple.lru.cache</groupId>
    <artifactId>JavaSimpleLRUCache</artifactId>
    <version>1.0</version>
</dependency>
```

## To do:
* Persist cache on file system.
* Asynch load cache item if invalid and return cached entry immediately.
