package com.sg.simple.lru.cache;

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
    private final String dataDir;

    public LRUCache(int cachesize, String dataDir) {
        super((int) Math.ceil(cachesize / loadfactor), loadfactor, true);
        this.cachesize = cachesize;
        this.dataDir = dataDir;
    }

    @Override
    protected boolean removeEldestEntry(Entry<K, V> eldest) {  
        deleteFromFilesystem(eldest);
        return this.size() > this.cachesize;        
    }
    
    private void deleteFromFilesystem(Entry<K, V> eldest){
        try{
            if(null == dataDir) return;
            new File(dataDir + "/" + eldest.getKey()).delete();
        }catch(Exception e){}
    }
    
    static void p(Object o){
        System.out.println(o);
    }

}
