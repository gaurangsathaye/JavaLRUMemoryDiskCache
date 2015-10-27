package com.sg.simple.lru.cache;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author sathayeg
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private static final float loadfactor = 0.75f;
    private final int cachesize;

    public LRUCache(int cachesize) {
        super((int) Math.ceil(cachesize / loadfactor), loadfactor, true);
        this.cachesize = cachesize;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {
        return this.size() > this.cachesize;
    }

}
