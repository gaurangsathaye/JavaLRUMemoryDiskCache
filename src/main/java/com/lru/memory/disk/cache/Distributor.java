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
        distribute(listenPort, cluster, DistributedConfig.getDefaultConfig(), caches);
    }
    
    /**
     * 
     * @param listenPort The port number for this JVM's cache server
     * @param cluster All your distributed caches: Format must be in the IP/DNS:port format for example: 172.16.0.0:8000,myserver.com:8000,172.17.0.0:9000.  All the caches in this cluster must be passed the same list with the same values and in the same order.
     * @param config
     * @param caches
     * @throws Exception 
     */
    public static void distribute(int listenPort, String cluster, DistributedConfig config, AbstractCacheService<? extends Serializable>... caches) throws Exception {
        if(distMgrMap.containsKey(Integer.toString(listenPort))){
            throw new Exception("Distributed caches already created on port: " + listenPort);
        }
        
        DistributedManager distributedManager = DistributedManager.getDistributedManagerForInApp(listenPort, cluster, config, caches);
        distMgrMap.put(Integer.toString(listenPort), distributedManager);
        
        distributedManager.startServer();
    }   
}
