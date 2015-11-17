package com.lru.memory.disk.cache;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 *
 * @author sathayeg
 * @param <K>
 * @param <V>
 */
class LRUCache<K extends String, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private static final float loadfactor = 0.75f;
    private final int cachesize;
    private final DiskOps diskOps;

    LRUCache(int cachesize, DiskOps diskOps) {
        super((int) Math.ceil(cachesize / loadfactor), loadfactor, true);
        this.cachesize = cachesize;
        this.diskOps = diskOps;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {        
        if(this.size() > this.cachesize){
            if(this.diskOps.isDiskPersistent()) deleteFromFilesystem(eldest);
            return true;
        }        
        return false;
    }
    
    private void deleteFromFilesystem(Entry<K, V> eldest){
        try{            
            this.diskOps.asyncDelete(eldest.getKey());
        }catch(Exception e){}
    }
}
