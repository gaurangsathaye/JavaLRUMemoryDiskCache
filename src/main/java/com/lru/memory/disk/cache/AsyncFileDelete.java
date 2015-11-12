package com.lru.memory.disk.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class AsyncFileDelete implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(AsyncFileDelete.class);
    
    private final String pathAndFilename;

    public AsyncFileDelete(String pathAndFilename) {
        this.pathAndFilename = pathAndFilename;
    }    

    @Override
    public void run() {
        try{
            Utl.deleteFile(this.pathAndFilename);
        }catch(Exception e){
            log.error("Unable to delete file: " + pathAndFilename, e);
        }
    }
    
}
