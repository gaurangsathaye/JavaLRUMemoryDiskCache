package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
public class DistributedConfig {
    
    public static final long SevereServerErrorAttemptDeltaMillis = 90000L;
    public static final long NetworkErrorAttemptDeltaMillis = 20000L;
    
    private static final int defaultClientConnectTimeoutMillis = 5000;
    private static final int defaultClientReadTimeoutMillis = 15000;
    private static final int defaultServerThreadPoolSize = 200;
    
    private final int serverThreadPoolSize;
    private final int clientConnTimeoutMillis;
    private final int clientReadTimeoutMillis;
    
    public DistributedConfig(int serverThreadPoolSize, int clientConnTimeoutMillis, int clientReadTimeoutMillis){
        
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
  
}
