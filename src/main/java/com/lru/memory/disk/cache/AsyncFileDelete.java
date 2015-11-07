package com.lru.memory.disk.cache;

import static com.lru.memory.disk.cache.Utl.p;

/**
 *
 * @author sathayeg
 */
public class AsyncFileDelete implements Runnable {
    
    private final String pathAndFilename;

    public AsyncFileDelete(String pathAndFilename) {
        this.pathAndFilename = pathAndFilename;
    }    

    @Override
    public void run() {
        try{
            Utl.deleteFile(this.pathAndFilename);
        }catch(Exception e){
            p("AsyncDeleteFile error: " + e);
        }
    }
    
}
