package com.lru.memory.disk.cache.distribute;

import java.io.Serializable;

/**
 *
 * @author sathayeg
 * @param <T>
 */
public class ClientServerRequestResponse<T extends Serializable> implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private final String clientSetServerHost;
    private final String clientSetCacheKey;
    private final String clientSetCacheName;
    
    private T serverSetData;
    private boolean serverError = false;
    private boolean serverResponse = false;
    private String serverErrorMessage;
    
    public ClientServerRequestResponse(String clientSetServerHost, String clientSetCacheKey, String clientSetCacheName){
        this.clientSetServerHost = clientSetServerHost;
        this.clientSetCacheKey = clientSetCacheKey;
        this.clientSetCacheName = clientSetCacheName;
    }

    public T getServerSetData() {
        return serverSetData;
    }

    public void setServerSetData(T serverSetData) {
        this.serverSetData = serverSetData;
    }

    public String getClientSetServerHost() {
        return clientSetServerHost;
    }

    public String getClientSetCacheKey() {
        return clientSetCacheKey;
    }

    public String getClientSetCacheName() {
        return clientSetCacheName;
    }

    public boolean isServerError() {
        return serverError;
    }

    public void setServerError(boolean serverError) {
        this.serverError = serverError;
    }   

    public boolean isServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(boolean serverResponse) {
        this.serverResponse = serverResponse;
    }  

    public String getServerErrorMessage() {
        return serverErrorMessage;
    }

    public void setServerErrorMessage(String serverErrorMessage) {
        this.serverErrorMessage = serverErrorMessage;
    }   
}
