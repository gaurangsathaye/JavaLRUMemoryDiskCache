package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author sathayeg
 */
class DistributedManager {
        
    private int serverPort;
    private final List<DistributedConfigServer> clusterServers = new ArrayList<>();
    private final Map<String, DistributedConfigServer> clusterServerMap = new HashMap<>();
    private final Map<String, AbstractCacheService<? extends Serializable>> cacheMap = new HashMap<>();
    private final DistributedServer server;
    private final String clusterConfig;
    private final AtomicBoolean foundSelf = new AtomicBoolean(false);
    private final DistributedConfig config;

    
    private int numberOfClusterServers;
    private DistributedConfigServer selfServer;
    private boolean standAlone = false;
    private DistributedClient distributedClient;

    DistributedManager(int serverPort, String clusterConfig, DistributedConfig config, AbstractCacheService<? extends Serializable>[] caches) throws Exception {
        
        if(Utl.areBlank(clusterConfig)) throw new Exception("Cluster config is blank.");
        
        if((null == caches) || (caches.length < 1)) throw new Exception("No caches passed in.");
        
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        if(null == config) throw new Exception("Config is null, please pass in Config");
        
        this.serverPort = serverPort;
        this.clusterConfig = clusterConfig;        
        this.config = config;
        this.standAlone = false; 
        
        this.server = new DistributedServer(serverPort, this);
        //Don't start server here, server is started by Distributor
        
        this.distributedClient = new DistributedClient(this);
        
        //These need to happen last because createCacheMap depends on this.distributedClient
        createClusterServers(clusterConfig);
        createCacheMap(caches);       
    }
    
    Runnable getServerRequestProcessor(Socket socket, DistributedManager distributedManager){
        if(this.standAlone){
            return null;
        }else{
            return new DistributedServerRequestProcessor(socket, distributedManager);
        }
    }
    
    DistributedConfigServer getClusterServerForCacheKey(String key) throws BadRequestException{
        if(Utl.areBlank(key)) throw new BadRequestException("key is blank", null);
        return clusterServers.get((Math.abs(key.hashCode()) % this.numberOfClusterServers));
    }
    
    DistributedConfig getConfig(){
        return this.config;
    }
    
    DistributedClient getDistributedClient(){
        return this.distributedClient;
    }
    
    int getServerPort() {
        return serverPort;
    }

    List<DistributedConfigServer> getClusterServers() {
        return clusterServers;
    }

    Map<String, AbstractCacheService<? extends Serializable>> getCacheMap() {
        return cacheMap;
    }   
    
    boolean getFoundSelf(){
        return this.foundSelf.get();
    }

    DistributedConfigServer getSelfServer() {
        return selfServer;
    }   
    
    synchronized boolean setSelfOnClusterServers(String serverHostFromClient){
        if(foundSelf.get()) return true;
        if(Utl.areBlank(serverHostFromClient)) return false;
        serverHostFromClient = serverHostFromClient.trim().toLowerCase();
        
        String key = DistributedConfigServer.getServerId(serverHostFromClient, serverPort);
        if(Utl.areBlank(key)) return false;
        
        DistributedConfigServer dcs = clusterServerMap.get(key);
        if(null == dcs) return false;
        
        dcs.setSelf(true);
        foundSelf.set(true);
        this.selfServer = dcs;
        
        return foundSelf.get();
    }
      
    void startServer() {
        new Thread(this.server).start();
    }
    
    private void createClusterServers(String cluster) throws Exception {
        String[] hostPorts = cluster.split(",");
        for(String hostPort:hostPorts){
            if(Utl.areBlank(hostPort)) throw new Exception("Cluster member blank, invalid cluster config");
            String [] hostAndPort = hostPort.split(":");
            if(hostAndPort.length < 2) throw new Exception("Invalid cluster member: " + hostPort);
            clusterServers.add(new DistributedConfigServer(hostAndPort[0], hostAndPort[1]));
        }
        
        boolean match = false;
        for(DistributedConfigServer cs : clusterServers){
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
        
        for(DistributedConfigServer dcs : this.clusterServers){
            String key = dcs.getServerId();
            if(clusterServerMap.containsKey(key)) {
                throw new Exception("Duplicate cluster servers in cluster config: " + key);
            }else{
                clusterServerMap.put(key, dcs);
            }            
        }
    }
    
    private void createCacheMap(AbstractCacheService<? extends Serializable>... caches) {
        for(AbstractCacheService<? extends Serializable> cache : caches){
            try{cache.setDistributedManager(this);}catch(Exception e){}
            cacheMap.put(cache.getCacheName(), cache);
        }
    }
}
