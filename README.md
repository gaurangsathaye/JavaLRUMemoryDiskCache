# JavaSimpleLRUCache

This is a thread safe, easy to use Java LRU cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object and loads it in the cache if not present. You instruct how object is loaded.  There are other methods like 'getOnly' and 'putOnly', but this is probably the only method you need.   
* `public final Map<String, Object> getStats()` - Get stats for your cache like hit ratio, cache size, hits, misses, etc.

Here are some of the basics:  
(See the `com.example.sg.simple.lru` package for an example on how to use. Start with `Example1.java`)

For example, create your cache like this:  
`public class ExampleCache1 extends AbstractCacheService<ExampleMyObjectToCache>`

In your cache, override the following two methods: (You do not call the `isCacheItemValid` and `loadData` methods directly.  You just need to define them, and they will be called by the internal cache as needed.
```java
/*
    You decide if your cached object is valid.

    You can use timestamps, last modified or any other parameters to determine
    if you cached object is valid.

    If you return true here, your cached object will be returned.
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
    return your_api_or_dao.loadObject(key);
}
```

Other points:  
* The cache key must be a string. 
* You cannot store null values in the cache. So your `loadData(String key)` method should not return a null object.  Otherwise `public T get(String key)` will throw an exception.  
* The `com.sg.simple.lru.cache.CacheEntry` object is a utility wrapper object you can store your real object in.  It has a default timestamp for when the object is created.  ie: `public class ExampleCache extends AbstractCacheService<CacheEntry<YourObjectToCache>>`
