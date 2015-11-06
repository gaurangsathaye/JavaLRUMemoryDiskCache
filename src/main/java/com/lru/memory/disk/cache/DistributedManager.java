package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
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
    private final List<DistributedConfigServer> clusterServers = new ArrayList<>();
    private final Map<String, DistributedConfigServer> clusterServerMap = new HashMap<>();
    private final Map<String, AbstractCacheService<? extends Serializable>> cacheMap = new HashMap<>();
    private final DistributedServer server;
    private final String clusterConfig;
    private final AtomicBoolean foundSelf = new AtomicBoolean(false);
    
    private int numberOfClusterServers;
    private DistributedConfigServer selfServer;
    
    public DistributedManager(int serverPort, String clusterConfig, AbstractCacheService<? extends Serializable>... caches) throws Exception {
        if(Utl.areBlank(clusterConfig)) throw new Exception("Cluster config is blank.");
        
        if((null == caches) || (caches.length < 1)) throw new Exception("No caches passed in.");
        
        if(serverPort < 1) throw new Exception("Invalid port number: " + serverPort + ", serverPort must be between 1 and 65535 inclusive.");
        
        this.clusterConfig = clusterConfig;
        this.serverPort = serverPort;
        createClusterServers(clusterConfig);
        createCacheMap(caches);
        
        this.server = new DistributedServer(serverPort, this);
        startServer();
    }
    
    public DistributedConfigServer getClusterServerForCacheKey(String key) throws BadRequestException{
        if(Utl.areBlank(key)) throw new BadRequestException("key is blank", null);
        return clusterServers.get((Math.abs(key.hashCode()) % this.numberOfClusterServers));
    }
    
    public int getServerPort() {
        return serverPort;
    }

    public List<DistributedConfigServer> getClusterServers() {
        return clusterServers;
    }

    public Map<String, AbstractCacheService<? extends Serializable>> getCacheMap() {
        return cacheMap;
    }   
    
    boolean getFoundSelf(){
        return this.foundSelf.get();
    }

    public DistributedConfigServer getSelfServer() {
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
    
    
    DistributedRequestResponse<Serializable> distributedCacheGet(String cacheName, String key, 
            DistributedConfigServer clusterServerForCacheKey) throws BadRequestException, SocketException, IOException, ClassNotFoundException { 
        if(Utl.areBlank(cacheName, key)) throw new BadRequestException("DistributedManger.distributedCacheGet: cacheName, key is blank", null);
        if(null == clusterServerForCacheKey) throw new BadRequestException("clusterServerForCacheKey is null", null);
        InputStream is = null;
        OutputStream os = null;
        
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        Socket clientSock = null;

        AutoCloseable closeables[] = {is, os, oos, ois, clientSock};
        try {            
            //clientSock = new Socket(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort());
            clientSock = new Socket();
            clientSock.connect(new InetSocketAddress(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort()), 2);
            clientSock.setSoTimeout(5);
            DistributedRequestResponse<Serializable> distrr = 
                    new DistributedRequestResponse<>(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort(),
                            key, cacheName);
            
            os = clientSock.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(distrr);
            oos.flush(); 
            
            is = clientSock.getInputStream();
            ois = new ObjectInputStream(is);
            DistributedRequestResponse<Serializable> resp = (DistributedRequestResponse<Serializable>) ois.readObject();
            return resp;
        } finally {
            Utl.closeAll(closeables);
        }
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
            String key = dcs.toString();
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
