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
    
    public static final byte ServerErrorLevelSevere_CacheNameDoesNotExist = 23;
    public static final byte ServerErrorLevelSevere_SelfIdentification = 22;
    public static final byte ServerErrorLevelSevere_ClientServerDontMatchForKey = 21; //If the distributed server the client thinks should serve this key does not match the distributed server the server thinks should handle this key.
    public static final byte ServerErrorLevelSevere = 20;
    public static final byte ServerErrorLevelBadRequest = 7;
    public static final byte ServerErrorLevelCacheGetException = 2;
    public static final byte ServerErrorLevelCacheGetAllOk = 1;
    public static final byte ServerErrorLevelNotSet = 0;
        
    private ServerSocket serverSocket = null;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private final int port;   
    private final DistributedManager distributedManager;

    DistributedServer(int port, DistributedManager distributedManager) {
        this.port = port;
        this.distributedManager = distributedManager;
    }

    void startServer() throws IOException {
        try{
            this.serverSocket = new ServerSocket(port);
            p("DistributedServer started on port: " + port);
            while(true){
                try{
                    this.threadPool.execute(new DistributedServerRequestProcessor(serverSocket.accept(), this.distributedManager));
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
    
    static void p(Object o){
        System.out.println(o);
    }
}
