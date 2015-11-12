package com.lru.memory.disk.cache;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class AsyncFileSerialize implements Runnable{
    
    private static final Logger log = LoggerFactory.getLogger(AsyncFileSerialize.class);

    
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
            log.error("Unable to serialize file: " + pathAndFilename, e);
        }
    }
    
}
