package com.lru.memory.disk.cache;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author sathayeg
 */
class DistributedServer implements Runnable {
    
    static final byte ServerErrorLevelSevere_CacheNameDoesNotExist = 23;
    static final byte ServerErrorLevelSevere_SelfIdentification = 22;
    static final byte ServerErrorLevelSevere_ClientServerDontMatchForKey = 21; //If the distributed server the client thinks should serve this key does not match the distributed server the server thinks should handle this key.
    static final byte ServerErrorLevelSevere = 20;
    
    static final byte ServerErrorLevelBadRequest = 7;

    //We don't want to cache distributed responses where error level is over ServerErrorLevelDistributedResponseCacheable, becaues it will interfere with deltas for servere and network errors
    static final byte ServerErrorLevelDistributedResponseCacheable = 2;
    static final byte ServerErrorLevelCacheGetException = 2;
    static final byte ServerErrorLevelCacheGetAllOk = 1;
    static final byte ServerErrorLevelNotSet = 0;
        
    private ServerSocket serverSocket = null;
    private ExecutorService threadPool = null;
    private final int port;   
    private final DistributedManager distributedManager;

    DistributedServer(int port, DistributedManager distributedManager) {
        this.port = port;
        this.distributedManager = distributedManager;
        threadPool = Executors.newFixedThreadPool(this.distributedManager.getConfig().getServerThreadPoolSize());
    }

    void startServer() throws IOException {
        try{
            this.serverSocket = new ServerSocket(port);
            while(true){
                try{
                    //this.threadPool.execute(new DistributedServerRequestProcessor(serverSocket.accept(), this.distributedManager));
                    this.threadPool.execute(this.distributedManager.getServerRequestProcessor(serverSocket.accept(), distributedManager));
                }catch(Exception e){
                }                
            }
        } catch (IOException ex) {
            try{this.serverSocket.close();}catch(Exception e){}
            throw ex;
        }
    }

    @Override
    public void run() {
        try{
            startServer();
        }catch(Exception e){
            String err = "Unable to start server on port: " + port + " : " + e.getMessage() + ", cause: " + e.getCause();
            throw new RuntimeException(err, e);
        }
    }
}
