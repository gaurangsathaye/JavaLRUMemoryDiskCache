package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
public class DistributedConfig {
    
    static final long SevereServerErrorAttemptDeltaMillis = 90000L;
    static final long NetworkErrorAttemptDeltaMillis = 20000L;
    //Don't set DistributedCachedEntryTtlMillis higher than NetworkErrorAttemptDeltaMillis and SevereServerErrorAttemptDeltaMillis, 
    //because it will interfere with deltas for DistributedConfigServer.tryRemote
    static final long DistributedCachedEntryTtlMillis = NetworkErrorAttemptDeltaMillis;
    
    private static final int defaultClientConnectTimeoutMillis = 5000;
    private static final int defaultClientReadTimeoutMillis = 15000;
    private static final int defaultServerThreadPoolSize = 200;
    private static final boolean defaultCacheDistributedResponse = true;
    
    private final int serverThreadPoolSize;
    private final int clientConnTimeoutMillis;
    private final int clientReadTimeoutMillis;
    private final boolean cacheDistributedResponse;
    
    /**
     * 
     * @param serverThreadPoolSize
     * @param clientConnTimeoutMillis
     * @param clientReadTimeoutMillis
     * @param cacheDistributedResponse whether distributed response should be cached for a short time (20 seconds), to improve performance from fewer network calls
     */
    public DistributedConfig(int serverThreadPoolSize, int clientConnTimeoutMillis, int clientReadTimeoutMillis, boolean cacheDistributedResponse){
        
        if(serverThreadPoolSize < 1){
            serverThreadPoolSize = getDefaultServerThreadPoolSize();
        }
        
        if(clientConnTimeoutMillis < 0){
            clientConnTimeoutMillis = getDefaultClientConnectTimeoutMillis();
        }
        
        if(clientReadTimeoutMillis < 0){
            clientReadTimeoutMillis = getDefaultClientReadTimeoutMillis();
        }
        
        this.serverThreadPoolSize = serverThreadPoolSize;
        this.clientConnTimeoutMillis = clientConnTimeoutMillis;
        this.clientReadTimeoutMillis = clientReadTimeoutMillis;
        this.cacheDistributedResponse = cacheDistributedResponse;
    }

    public static boolean isDefaultCacheDistributedResponse() {
        return defaultCacheDistributedResponse;
    }

    public boolean isCacheDistributedResponse() {
        return cacheDistributedResponse;
    }   

    public static long getSevereServerErrorAttemptDeltaMillis() {
        return SevereServerErrorAttemptDeltaMillis;
    }

    public static long getNetworkErrorAttemptDeltaMillis() {
        return NetworkErrorAttemptDeltaMillis;
    }

    public static int getDefaultClientConnectTimeoutMillis() {
        return defaultClientConnectTimeoutMillis;
    }

    public static int getDefaultClientReadTimeoutMillis() {
        return defaultClientReadTimeoutMillis;
    }

    public static int getDefaultServerThreadPoolSize() {
        return defaultServerThreadPoolSize;
    }

    public int getServerThreadPoolSize() {
        return serverThreadPoolSize;
    }

    public int getClientConnTimeoutMillis() {
        return clientConnTimeoutMillis;
    }

    public int getClientReadTimeoutMillis() {
        return clientReadTimeoutMillis;
    }
    
    /**
     * 
     * @return 
     */
    public static DistributedConfig getDefaultConfig() {
        return new DistributedConfig(getDefaultServerThreadPoolSize(),
                getDefaultClientConnectTimeoutMillis(), 
                getDefaultClientReadTimeoutMillis(),
                isDefaultCacheDistributedResponse()
        );
    }
  
}
