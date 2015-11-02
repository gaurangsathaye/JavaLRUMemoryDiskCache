package com.lru.memory.disk.cache.distribute;

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
public class ServerRequestProcessor implements Runnable {    
    private final Socket clientSocket;
    private final DistributedManager distMgr;
    
    public ServerRequestProcessor(Socket clientSocket, DistributedManager dm) {
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
            ClientServerRequestResponse<Serializable> csrr = (ClientServerRequestResponse<Serializable>) ois.readObject();
            String cacheKey = csrr.getClientSetCacheKey();
            String cacheName = csrr.getClientSetCacheName();
            String serverHost = csrr.getClientSetServerHost();
            
            if(Utl.areBlank(cacheKey, cacheName, serverHost)){
                csrr.setServerError(true);
                csrr.setServerErrorMessage("cacheKey, cacheName or serverHost is blank");
                os = clientSocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(csrr);
                oos.flush();
                return;
            }
            
            if( (! this.distMgr.getFoundSelf()) &&  (! this.distMgr.setSelfOnClusterServers(serverHost)) ){
                csrr.setServerError(true);
                csrr.setServerErrorMessage("Unable to self detect for serverHost: " + serverHost);
                os = clientSocket.getOutputStream();
                oos = new ObjectOutputStream(os);
                oos.writeObject(csrr);
                oos.flush();
                return;
            }
            
            ClusterServer clusterServer = this.distMgr.getClusterServerForCacheKey(cacheKey);
            csrr.setServerSetData(clusterServer.toString());
            csrr.setServerResponse(true);
            
            os = clientSocket.getOutputStream();
            oos = new ObjectOutputStream(os);
            oos.writeObject(csrr);
            oos.flush(); 
        }catch(Exception e){
        }finally{
            Utl.closeAll(closeables);
        }
    }   
}
