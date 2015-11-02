package com.lru.memory.disk.cache.distribute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author sathayeg
 */
public class Server implements Runnable {
        
    private ServerSocket serverSocket = null;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    private final int port;   
    private final DistributedManager distributedManager;

    public Server(int port, DistributedManager distributedManager) {
        this.port = port;
        this.distributedManager = distributedManager;
    }

    public void startServer() throws IOException {
        try{
            this.serverSocket = new ServerSocket(port);
            while(true){
                try{
                    this.threadPool.execute(new ServerRequestProcessor(serverSocket.accept(), this.distributedManager));
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
