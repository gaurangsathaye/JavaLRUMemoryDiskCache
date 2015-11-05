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
            String serverHost = distrr.getClientSetServerHost();
            
            distrr.setServerResponse(true);
            
            if(Utl.areBlank(cacheKey, cacheName, serverHost)){
                distrr.setServerErrorMessage("cacheKey, cacheName or serverHost is blank");
                os = clientSocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(distrr);
                oos.flush();
                return;
            }
            
            if( (! this.distMgr.getFoundSelf()) &&  (! this.distMgr.setSelfOnClusterServers(serverHost)) ){
                distrr.setServerError(true);
                distrr.setServerErrorMessage("Unable to self detect for serverHost: " + serverHost);
                os = clientSocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(distrr);
                oos.flush();
                return;
            }
            
            distrr.setServerId(this.distMgr.getSelfServer().toString());
            distrr.setServerSetData(this.distMgr.getCacheMap().get(cacheName).getNonDistributed(cacheKey));
            
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
