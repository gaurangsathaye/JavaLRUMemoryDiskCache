package com.lru.memory.disk.cache;

import java.io.Serializable;

/**
 *
 * @author sathayeg
 * @param <T>
 */
public class DistributedRequestResponse<T extends Serializable> implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private final String clientSetServerHost;
    private final int clientSetServerPort;
    private final String clientSetCacheKey;
    private final String clientSetCacheName;
    
    private T serverSetData;
    private byte serverSetErrorLevel = DistributedServer.ServerErrorLevelNotSet;
    private String serverSetErrorMessage;
    private String serverSetServerId;
    private String serverSetServerToHandleKey;
    
    
    public DistributedRequestResponse(String clientSetServerHost, int clientSetServerPort,
            String clientSetCacheKey, String clientSetCacheName){
        this.clientSetServerHost = clientSetServerHost;
        this.clientSetServerPort = clientSetServerPort;
        this.clientSetCacheKey = clientSetCacheKey;
        this.clientSetCacheName = clientSetCacheName;
    }

    public String getClientSetServerHost() {
        return clientSetServerHost;
    }

    public int getClientSetServerPort() {
        return clientSetServerPort;
    }

    public String getClientSetCacheKey() {
        return clientSetCacheKey;
    }

    public String getClientSetCacheName() {
        return clientSetCacheName;
    }    

    public T getServerSetData() {
        return serverSetData;
    }

    public void setServerSetData(T serverSetData) {
        this.serverSetData = serverSetData;
    }

    public byte getServerSetErrorLevel() {
        return serverSetErrorLevel;
    }

    public void setServerSetErrorLevel(byte serverSetErrorLevel) {
        this.serverSetErrorLevel = serverSetErrorLevel;
    }

    public String getServerSetErrorMessage() {
        return serverSetErrorMessage;
    }

    public void setServerSetErrorMessage(String serverSetErrorMessage) {
        this.serverSetErrorMessage = serverSetErrorMessage;
    }

    public String getServerSetServerId() {
        return serverSetServerId;
    }

    public void setServerSetServerId(String serverSetServerId) {
        this.serverSetServerId = serverSetServerId;
    }

    public String getServerSetServerToHandleKey() {
        return serverSetServerToHandleKey;
    }

    public void setServerSetServerToHandleKey(String serverSetServerToHandleKey) {
        this.serverSetServerToHandleKey = serverSetServerToHandleKey;
    }

    @Override
    public String toString() {
        return "DistributedRequestResponse{" + "clientSetServerHost=" + clientSetServerHost + ", clientSetServerPort=" + clientSetServerPort + ", clientSetCacheKey=" + clientSetCacheKey + ", clientSetCacheName=" + clientSetCacheName + ", serverSetErrorLevel=" + serverSetErrorLevel + ", serverSetErrorMessage=" + serverSetErrorMessage + ", serverSetServerId=" + serverSetServerId + ", serverSetServerToHandleKey=" + serverSetServerToHandleKey + '}';
    }    
}
