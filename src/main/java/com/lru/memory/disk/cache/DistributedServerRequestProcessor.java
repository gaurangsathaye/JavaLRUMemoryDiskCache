package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.Utl;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 *
 * @author sathayeg
 */
class DistributedServerRequestProcessor implements Runnable {
    
    private final Socket clientSocket;
    private final DistributedManager distMgr;
    
    public DistributedServerRequestProcessor(Socket clientSocket, DistributedManager dm) {
        this.clientSocket = clientSocket;
        this.distMgr = dm;
    }

    @Override
    public void run() {   
        InputStream is = null;        
        OutputStream os = null;
        
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        
        AutoCloseable closeables[] = {is, os, oos, ois, clientSocket};
        
        try{            
            is = clientSocket.getInputStream();           
            ois = new ObjectInputStream(is);
            DistributedRequestResponse<Serializable> distrr = (DistributedRequestResponse<Serializable>) ois.readObject();
            String cacheKey = distrr.getClientSetCacheKey();
            String cacheName = distrr.getClientSetCacheName();
            String clientSetServerHost = distrr.getClientSetServerHost();
            int clientSetServerPort = distrr.getClientSetServerPort();
                        
            boolean sendError = false;
            
            if( (! sendError) && Utl.areBlank(cacheKey, cacheName, clientSetServerHost)){
                distrr.setServerSetErrorMessage("cacheKey, cacheName or serverHost is blank");
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelBadRequest);                
                sendError = true;                
            }
            
            if((! sendError) && (! this.distMgr.getFoundSelf()) &&  (! this.distMgr.setSelfOnClusterServers(clientSetServerHost)) ){
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelSevere_SelfIdentification);
                distrr.setServerSetErrorMessage("Unable to self detect for serverHost, possible config errors on: " + clientSetServerHost);                
                sendError = true;
            }else{
                distrr.setServerSetServerId(this.distMgr.getSelfServer().toString());
            }
            
            AbstractCacheService<? extends Serializable> cache = this.distMgr.getCacheMap().get(cacheName);
            if( (! sendError) && (null == cache) ){
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelSevere_CacheNameDoesNotExist);
                distrr.setServerSetErrorMessage("Cache for cache name: " + cacheName + " is null on: " + clientSetServerHost);                
                sendError = true;
            }
            
            DistributedConfigServer serverSideClusterServerForCacheKey = this.distMgr.getClusterServerForCacheKey(cacheKey);
            if( (! sendError) && (! serverSideClusterServerForCacheKey.toString().equals(DistributedConfigServer.getServerId(clientSetServerHost, clientSetServerPort))) ){
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelSevere_ClientServerDontMatchForKey);
                distrr.setServerSetErrorMessage("Cache for cache name: " + cacheName + " is null on: " + clientSetServerHost);                
                sendError = true;
            }
            
            if(sendError){
                os = clientSocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(distrr);
                oos.flush();
                return;
            }
            
            try{
               cache.getNonDistributed(cacheKey);
            }catch(Exception e){
                
            }
            
            os = clientSocket.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(distrr);
            oos.flush(); 
        }catch(Exception e){
        }finally{
            Utl.closeAll(closeables);
        }
    }   
}
