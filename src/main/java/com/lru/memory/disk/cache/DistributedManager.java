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
    private List<DistributedConfigServer> clusterServers = new ArrayList<>();
    private Map<String, DistributedConfigServer> clusterServerMap = new HashMap<>();
    private Map<String, AbstractCacheService<? extends Serializable>> cacheMap = new HashMap<>();
    private DistributedServer server;
    private String clusterConfig;
    private AtomicBoolean foundSelf = new AtomicBoolean(false);
    private DistributedConfig config;

    
    private int numberOfClusterServers;
    private DistributedConfigServer selfServer;
    private boolean standAlone = false;
    private DistributedClient distributedClient;
    
    private ServerCache standAloneServerCache;
    
    /*
        Sample cluster config: "127.0.0.1:19000, 127.0.0.1:19001"
    */    
    static DistributedManager getDistributedManagerForInApp(int serverPort, String clusterConfig, DistributedConfig config, AbstractCacheService<? extends Serializable>[] caches) throws Exception {
        return new DistributedManager(serverPort, clusterConfig, config, caches);
    }
    
    static DistributedManager getDistributedManagerForStandaloneServer(int serverPort, DistributedConfig config, ServerCache standAloneServerCache) throws Exception{
        return new DistributedManager(serverPort, config, standAloneServerCache);
    }
    
    static DistributedManager getDistributedManagerForStandaloneClient(String clusterConfig, DistributedConfig config) throws Exception{
        return new DistributedManager(clusterConfig, config);
    }

    /**
     * 
     * 
     * FOR IN-APP DISTRIBUTED CACHING
     *
     * 
     **/
    private DistributedManager(int serverPort, String clusterConfig, DistributedConfig config, AbstractCacheService<? extends Serializable>[] caches) throws Exception {
        
        if(Utl.areBlank(clusterConfig)) throw new Exception("Cluster config is blank.");
        
        if((null == caches) || (caches.length < 1)) throw new Exception("No caches passed in.");
        
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        if(null == config) throw new Exception("Config is null, please pass in Config");
        
        this.serverPort = serverPort;
        this.clusterConfig = clusterConfig;        
        this.config = config;
        this.standAlone = false; 
        
        this.server = new DistributedServer(this);
        //Don't start server here, server is started by Distributor
        
        this.distributedClient = new DistributedClient(this);
        
        //These need to happen last because createCacheMap depends on this.distributedClient
        createClusterServers(clusterConfig, true);
        createCacheMap(caches);       
    }
    
    /**
     * 
     * 
     * FOR STAND ALONE CACHE SERVER
     *
     * 
     **/
    private DistributedManager(int serverPort, DistributedConfig config, ServerCache standAloneServerCache) throws Exception{
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        if(null == config) throw new Exception("Config is null, please pass in Config");
        
        if(null == standAloneServerCache) throw new Exception("Stand alone server cache is null");
        
        this.serverPort = serverPort;
        this.config = config;
        this.standAlone = true;
        
        this.standAloneServerCache = standAloneServerCache;
        
        this.server = new DistributedServer(this);
    }
    
    /**
     * 
     * 
     * FOR STAND ALONE CACHE SERVER CLIENT
     *
     * 
     **/
    private DistributedManager(String clusterConfig, DistributedConfig config) throws Exception {
        if(Utl.areBlank(clusterConfig)) throw new Exception("Cluster config is blank.");    
        if(null == config) throw new Exception("Config is null, please pass in Config");
        
        this.clusterConfig = clusterConfig;        
        this.config = config;
               
        createClusterServers(clusterConfig, false);
    }

    
    Runnable getServerRequestProcessor(Socket socket, DistributedManager distributedManager){
        if(this.standAlone){
            return new ServerRequestProcessor(socket, this.standAloneServerCache);
        }else{
            return new DistributedServerRequestProcessor(socket, distributedManager);
        }
    }
    
    DistributedConfigServer getClusterServerForCacheKey(String key) throws BadRequestException{
        if(Utl.areBlank(key)) throw new BadRequestException("key is blank", null);
        return clusterServers.get((Math.abs(key.hashCode()) % this.numberOfClusterServers));
    }
    
    DistributedConfigServer getClusterServerForCacheKeyRotate(String key) throws BadRequestException{
        if(Utl.areBlank(key)) throw new BadRequestException("key is blank", null);
        
        int idx = Math.abs(key.hashCode()) % this.numberOfClusterServers;        
        DistributedConfigServer server = clusterServers.get(idx);
        if(server.tryRemote()) return server;
        
        for(int i=0;i<2;i++){
            ++idx;
            DistributedConfigServer otherServer = clusterServers.get(idx % this.numberOfClusterServers);
            if(otherServer.tryRemote()) return otherServer;
        }
        
        return server;
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
    
    private void createClusterServers(String cluster, boolean inApp) throws Exception {
        String[] hostPorts = cluster.split(",");
        for(String hostPort:hostPorts){
            if(Utl.areBlank(hostPort)) throw new Exception("Cluster member blank, invalid cluster config");
            String [] hostAndPort = hostPort.split(":");
            if(hostAndPort.length < 2) throw new Exception("Invalid cluster member: " + hostPort);
            clusterServers.add(new DistributedConfigServer(hostAndPort[0], hostAndPort[1]));
        }
        
        if(inApp){
            //Check that this distributed in app server port matches at least one port in the distributed config
            //If it doesn't, then cluster config is incorrect
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
