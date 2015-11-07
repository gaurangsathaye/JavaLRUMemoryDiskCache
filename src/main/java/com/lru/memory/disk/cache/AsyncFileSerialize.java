package com.lru.memory.disk.cache;

import static com.lru.memory.disk.cache.Utl.p;
import java.io.Serializable;

/**
 *
 * @author sathayeg
 */
public class AsyncFileSerialize implements Runnable{
    
    private final String pathAndFilename;
    private final Serializable obj;
    
    AsyncFileSerialize(String pathAndFilename, Serializable obj) {
        this.pathAndFilename = pathAndFilename;
        this.obj = obj;
    }

    @Override
    public void run() {
        try{
            Utl.serializeFile(pathAndFilename, obj);
        }catch(Exception e){
            p("AsyncSerialize error: " + e);
        }
    }
    
}
