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

/**
 *
 * @author sathayeg
 */
public class DistributedClient {
    private final DistributedManager distMgr;
    private final DistributedConfig config;
    
    DistributedClient(DistributedManager distMgr){
        this.distMgr = distMgr;
        this.config = this.distMgr.getConfig();
    }
    
    DistributedRequestResponse<Serializable> getCachedDistResponse(String cacheName, String key, 
            DistributedConfigServer clusterServerForCacheKey) {
        
        return null;
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
            clientSock.connect(new InetSocketAddress(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort()), this.config.getClientConnTimeoutMillis());
            clientSock.setSoTimeout(this.config.getClientReadTimeoutMillis());
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
    
}
