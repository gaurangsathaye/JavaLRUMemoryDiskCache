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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class DistributedClient {
    
    private static final Logger log = LoggerFactory.getLogger(DistributedClient.class);
    
    private final DistributedManager distMgr;
    private final DistributedConfig config;
    private final DistributedResponseCache distResponseCache;
    
    DistributedClient(DistributedManager distMgr) throws Exception{
        this.distMgr = distMgr;
        this.config = this.distMgr.getConfig();
        this.distResponseCache = new DistributedResponseCache("distRespCache", 3900);
    }
    
    DistributedRequestResponse<Serializable> distCacheGet(String cacheName, String key, 
            DistributedConfigServer clusterServerForCacheKey) throws BadRequestException, SocketException, IOException, ClassNotFoundException {
        if(config.isCacheDistributedResponse()){
            return internalGetCachedDistResponse(cacheName, key, clusterServerForCacheKey);
        }else{
            return internalDistributedCacheGet(cacheName, key, clusterServerForCacheKey);
        }
    }
    
    private static String getKeyForCacheDistResponse(String cacheName, String key){
        return (cacheName + key);
    }
    
    /**
     * 
     * Cache the distributed responses only if error level is below a certain amount.
     * 
     * @param cacheName
     * @param key
     * @param clusterServerForCacheKey
     * @return
     * @throws BadRequestException
     * @throws SocketException
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    private DistributedRequestResponse<Serializable> internalGetCachedDistResponse(String cacheName, String key, 
            DistributedConfigServer clusterServerForCacheKey) throws BadRequestException, SocketException, IOException, ClassNotFoundException {
        
        if(Utl.areBlank(cacheName, key)) throw new BadRequestException("DistributedManger.distributedCacheGet: cacheName, key is blank", null);
        if(null == clusterServerForCacheKey) throw new BadRequestException("clusterServerForCacheKey is null", null);
        
        CacheEntry<Serializable> cacheEntry = null;
        try{
            cacheEntry = distResponseCache.getOnly(getKeyForCacheDistResponse(cacheName, key));
            if( (null != cacheEntry) && (! cacheEntry.isTtlExpired()) ){
                Serializable ser = cacheEntry.getCached();
                if(null != ser){
                    try{
                        DistributedRequestResponse<Serializable> distrr = (DistributedRequestResponse<Serializable>) ser;
                        return distrr;
                    }catch(Exception e){
                        log.error("Unable to cast Serializable to DistributedRequestResponse for cache name: " + cacheName + ", key: " + key, e);
                    }
                }
            }else if( (null != cacheEntry) && (cacheEntry.isTtlExpired()) ){
                //("getCachedDistResponse, cacheEntry expired for cache name: " + cacheName + ", key: " + key);
            }
           
        }catch(Exception e){
            log.error("Unable to get from distResponseCache: " + cacheName + ", key: " + key , e);
        }
        
        DistributedRequestResponse<Serializable> distributedCacheGet = internalDistributedCacheGet(cacheName, key, clusterServerForCacheKey);        
        
        try{            
            if( (null != distributedCacheGet) && (distributedCacheGet.getServerSetErrorLevel() <= DistributedServer.ServerErrorLevelDistributedResponseCacheable) ){
                cacheEntry = null;
                cacheEntry = new CacheEntry<>(distributedCacheGet, System.currentTimeMillis());
                cacheEntry.setTtl(DistributedConfig.DistributedCachedEntryTtlMillis);
                distResponseCache.putOnly(getKeyForCacheDistResponse(cacheName, key), cacheEntry);
                //("getCachedDistResponse, put in distResponseCache for cache name: " + cacheName + ", key: " + key);
            }
        }catch(Exception e){
            log.error("Unable to put in distResponseCache for cache name: " + cacheName + ", key: " + key, e);
        }                
        
        return distributedCacheGet;
    }
    
    private DistributedRequestResponse<Serializable> internalDistributedCacheGet(String cacheName, String key, 
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
