package com.lru.memory.disk.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sathayeg
 */
public class Distributor {
    
    private static final Map<String, DistributedManager> distMgrMap = new HashMap<>();
    
    /**
     * Create a distributed cache for
     * 
     * 
     * @param listenPort The port number for this JVM's cache server
     * @param cluster All your distributed caches: Format must be in the IP/DNS:port format for example: 172.16.0.0:8000,myserver.com:8000,172.17.0.0:9000.  All the caches in this cluster must be passed the same list with the same values and in the same order.
     * @param caches
     * @throws Exception 
     */
    public static void distribute(int listenPort, String cluster, AbstractCacheService<? extends Serializable>... caches) throws Exception {
        if(distMgrMap.containsKey(Integer.toString(listenPort))){
            throw new Exception("Distributed caches already created on port: " + listenPort);
        }
        distMgrMap.put(Integer.toString(listenPort), new DistributedManager(listenPort, cluster, caches));
    }
    
    /*static int clientConnectTimeoutMillis = 5000;
    static int clientReadTimeoutMillis = 15000;
    static int serverThreadPoolSize = 200;*/    
    public static void config(int serverThreadPoolSize, int clientConnectTimeoutMillis, int clientReadTimeoutMillis){
        if(serverThreadPoolSize > 0){
            Config.serverThreadPoolSize = serverThreadPoolSize;
        }
        
        if(clientConnectTimeoutMillis > -1){
            Config.clientConnectTimeoutMillis = clientConnectTimeoutMillis;
        }
        
        if(clientReadTimeoutMillis > -1){
            Config.clientReadTimeoutMillis = clientReadTimeoutMillis;
        }
    }
    
    public static DistributedManager getDistMgr(int serverPort){
        return distMgrMap.get(Integer.toString(serverPort));
    }
}
