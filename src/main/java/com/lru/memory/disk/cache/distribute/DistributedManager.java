package com.lru.memory.disk.cache.distribute;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.Utl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author sathayeg
 */
public class DistributedManager {
    
    private int serverPort;
    private final List<ClusterServer> clusterServers = new ArrayList<>();
    private final Map<String, AbstractCacheService<? extends Serializable>> cacheMap = new HashMap<>();
    private final Server server;
    private final String clusterConfig;
    private final AtomicBoolean foundSelf = new AtomicBoolean(false);
    
    private int numberOfClusterServers;
    
    public DistributedManager(int serverPort, String clusterConfig, AbstractCacheService<? extends Serializable>... caches) throws Exception {
        if(Utl.areBlank(clusterConfig)) throw new Exception("Cluster config is blank.");
        
        if((null == caches) || (caches.length < 1)) throw new Exception("No caches passed in.");
        
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        this.clusterConfig = clusterConfig;
        this.serverPort = serverPort;
        createClusterServers(clusterConfig);
        createCacheMap(caches);
        
        this.server = new Server(serverPort, this);
        startServer();
    }
    
    public ClusterServer getClusterServerForCacheKey(String key) throws Exception{
        if(Utl.areBlank(key)) throw new Exception("key is blank");
        return clusterServers.get((Math.abs(key.hashCode()) % this.numberOfClusterServers));
    }
    
    public boolean getFoundSelf(){
        return this.foundSelf.get();
    }
    
    public synchronized boolean setSelfOnClusterServers(String serverHostFromClient){
        if(foundSelf.get()) return true;
        if(Utl.areBlank(serverHostFromClient)) return false;
        serverHostFromClient = serverHostFromClient.trim().toLowerCase();
        for(ClusterServer cs:clusterServers){
            if(cs.getHost().equals(serverHostFromClient) && (cs.getPort()==serverPort)){
                cs.setSelf(true);
                foundSelf.set(true);
            }
        }
        return foundSelf.get();
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
        
        boolean match = false;
        for(ClusterServer cs : clusterServers){
            if(cs.getPort() == this.serverPort){
                match = true;
                break;
            }
        }
        if(! match){
            throw new Exception("No items in Cluster config: " + this.clusterConfig + ", contain server port: " + this.serverPort);
        }
        
        this.numberOfClusterServers = clusterServers.size();
        if(this.numberOfClusterServers < 1) throw new Exception("Number of created clusters servers from cluster config: " + this.clusterConfig + ", is invalid: " + this.numberOfClusterServers);
    }
    
    private void createCacheMap(AbstractCacheService<? extends Serializable>... caches) {
        for(AbstractCacheService<? extends Serializable> cache : caches){
            try{cache.setDistributedManager(this);}catch(Exception e){}
            cacheMap.put(cache.getCacheName(), cache);
        }
    }
}
