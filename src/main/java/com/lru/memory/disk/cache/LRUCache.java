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
public class LRUCache<K extends String, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = 1L;
    private static final float loadfactor = 0.75f;
    private final int cachesize;
    private final DirLocate dirLocate;

    public LRUCache(int cachesize, DirLocate dirLocate) {
        super((int) Math.ceil(cachesize / loadfactor), loadfactor, true);
        this.cachesize = cachesize;
        this.dirLocate = dirLocate;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {        
        if(this.size() > this.cachesize){
            deleteFromFilesystem(eldest);
            return true;
        }        
        return false;
    }
    
    private void deleteFromFilesystem(Entry<K, V> eldest){
        try{
            File f = new File(this.dirLocate.getPathToFile(eldest.getKey()));
            if(f.exists() && (! f.isDirectory())){
                f.delete();
            }
        }catch(Exception e){}
    }
}
