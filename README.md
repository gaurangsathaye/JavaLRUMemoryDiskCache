# JavaSimpleLRUCache

This is a thread safe, easy to use Java LRU cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object and loads it in the cache if not present. You instruct how object is loaded.  There are other methods like 'getOnly' and 'putOnly', but this is probably the only method you need.   
* `public final Map<String, Object> getStats()` - Get stats for your cache like hitratio, cache size, hits, misses, etc.

Here are some of the basics:  
(See the com.example.sg.simple.lru package for an example on how to use. Start with Example1.java)

For example, create your cache like this:  
`public class ExampleCache1 extends AbstractCacheService<ExampleMyObjectToCache>`

In your cache, override the following two methods:
```java
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
