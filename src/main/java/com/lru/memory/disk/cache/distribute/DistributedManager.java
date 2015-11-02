package com.lru.memory.disk.cache.distribute;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.Utl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author sathayeg
 */
public class DistributedManager {
    
    private int serverPort;
    private final List<ClusterServer> clusterServers = new ArrayList<>();
    private final Map<String, AbstractCacheService<? extends Serializable>> cacheMap = new HashMap<>();
    private final Server server;
    
    public DistributedManager(int serverPort, String cluster, AbstractCacheService<? extends Serializable>... caches) throws Exception {
        if(Utl.areBlank(cluster)) throw new Exception("Cluster config is blank.");
        
        if((null == caches) || (caches.length < 1)) throw new Exception("No caches passed in.");
        
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        this.serverPort = serverPort;
        createClusterServers(cluster);
        createCacheMap(caches);
        
        this.server = new Server(serverPort, this);
        startServer();
    }

    public int getServerPort() {
        return serverPort;
    }

    public List<ClusterServer> getClusterServers() {
        return clusterServers;
    }

    public Map<String, AbstractCacheService<? extends Serializable>> getCacheMap() {
        return cacheMap;
    }   
    
    private void startServer() {
        new Thread(this.server).start();
    }
    
    private void createClusterServers(String cluster) throws Exception {
        String[] hostPorts = cluster.split(",");
        for(String hostPort:hostPorts){
            if(Utl.areBlank(hostPort)) throw new Exception("Cluster member blank, invalid cluster config");
            String [] hostAndPort = hostPort.split(":");
            if(hostAndPort.length < 2) throw new Exception("Invalid cluster member: " + hostPort);
            clusterServers.add(new ClusterServer(hostAndPort[0], hostAndPort[1]));
        }
    }
    
    private void createCacheMap(AbstractCacheService<? extends Serializable>... caches) {
        for(AbstractCacheService<? extends Serializable> cache : caches){
            cacheMap.put(cache.getCacheName(), cache);
        }
    }
}
