package com.lru.memory.disk.cache;

/**
 *
 * @author sathayeg
 */
public class Config {
    
    public static final long SevereServerErrorAttemptDeltaMillis = 90000L;
    public static final long NetworkErrorAttemptDeltaMillis = 20000L;
    
    static int clientConnectTimeoutMillis = 5000;
    static int clientReadTimeoutMillis = 15000;
    static int serverThreadPoolSize = 200;

}
