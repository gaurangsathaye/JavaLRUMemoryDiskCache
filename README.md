# JavaSimpleLRUCache
Java Simple LRU Cache

This is a thread safe, easy to use Java LRU cache.  

Some of the benefits of using the cache are...  
* `public T get(String key)` - Gets your object and loads it in the cache if not present. You instruct how object is loaded.  There are other methods like 'getOnly' and 'putOnly', but this is probably the only method you need.   
* `public final Map<String, Object> getStats()` - Get stats for your cache like hitratio, cache size, hits, misses, etc.

See the com.example.sg.simple.lru package for details.  
Start with Example1.java

Here
