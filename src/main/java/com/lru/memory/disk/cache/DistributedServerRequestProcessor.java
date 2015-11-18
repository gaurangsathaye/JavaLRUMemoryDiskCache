package com.lru.memory.disk.cache;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
class DistributedServerRequestProcessor implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedServerRequestProcessor.class);
    
    private final Socket socket;
    private final DistributedManager distMgr;
    
    public DistributedServerRequestProcessor(Socket clientSocket, DistributedManager dm) {
        this.socket = clientSocket;
        this.distMgr = dm;
    }

    @Override
    public void run() {   
        InputStream is = null;        
        OutputStream os = null;
        
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        
        AutoCloseable closeables[] = {is, os, oos, ois, socket};
        
        try{            
            is = socket.getInputStream();           
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
                distrr.setServerSetServerId(this.distMgr.getSelfServer().getServerId());
            }
            
            AbstractCacheService<? extends Serializable> cache = this.distMgr.getCacheMap().get(cacheName);
            if( (! sendError) && (null == cache) ){
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelSevere_CacheNameDoesNotExist);
                distrr.setServerSetErrorMessage("Cache for cache name: " + cacheName + " is null on: " + clientSetServerHost);                
                sendError = true;
            }
            
            distrr.setServerSetServerToHandleKey(this.distMgr.getClusterServerForCacheKey(cacheKey).getServerId());
            String clientServerIdForKey = DistributedConfigServer.getServerId(clientSetServerHost, clientSetServerPort);
            if( (! sendError) && (! distrr.getServerSetServerToHandleKey().equals(clientServerIdForKey) ) ){
                //The server that client thinks should handle this key, and the server that the server thinks should handle the key are different.
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelSevere_ClientServerDontMatchForKey);
                distrr.setServerSetErrorMessage("cache key: " + cacheKey + ", client serverId for key: " + clientServerIdForKey +
                        ", server serverId for key: " + distrr.getServerSetServerToHandleKey());                
                sendError = true;
            }
            
            if(sendError){
                os = socket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(distrr);
                oos.flush();
                return;
            }
            
            try{
                distrr.setServerSetData(cache.getNonDistributed(cacheKey));
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelCacheGetAllOk);
            }catch(Exception e){
                log.error("Unable to getNonDistributed(), cache name: " + cacheName + ", key: " + cacheKey, e);
                distrr.setServerSetData(null);
                distrr.setServerSetErrorLevel(DistributedServer.ServerErrorLevelCacheGetException);
            }
            
            os = socket.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(distrr);
            oos.flush(); 
        }catch(Exception e){
            log.error("Server unable to process", e);
        }finally{
            Utl.closeAll(closeables);
        }
    }
}
