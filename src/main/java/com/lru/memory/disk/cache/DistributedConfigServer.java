package com.lru.memory.disk.cache;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author sathayeg
 */
public class DistributedConfigServer {
    
    private final String host;
    private final int port;
    private AtomicBoolean self = new AtomicBoolean(false);
    private long errNextAttemptTimestamp = 0L;
    
    private AtomicLong totalSevereErrors = new AtomicLong(0);
    private AtomicInteger severeErrorsRotate = new AtomicInteger(0);
    private int severeErrorRotateLimit = 7;
    
    private AtomicLong totalNetworkErrors = new AtomicLong(0);
    private AtomicInteger networkErrorsRotate = new AtomicInteger(0);
    private int networkErrorsRotateLimit = 10;

    public DistributedConfigServer(String host, String port) throws Exception {
        if (Utl.areBlank(host, port)) {
            throw new Exception("host and/or port is blank");
        }
        this.host = host.trim().toLowerCase();
        try {
            this.port = Integer.parseInt(port.trim());
        } catch (Exception e) {
            throw new Exception("Invalid cluster port: " + port);
        }
    }

    @Override
    public String toString() {
        return getServerId() + ", self: " + self.get() + 
                ", tryRemote: " + tryRemote() + 
                ", totalSevereErr: " + totalSevereErrors.get() + 
                ", totalNetworkErr: " + totalNetworkErrors.get();
    }  
    
    
    public static int rotate(AtomicInteger ai, int rotateIn) {
        ai.set(((ai.get() % rotateIn) + 1));
        return ai.get();
    }

    public boolean tryRemote() {
        return (System.currentTimeMillis() > errNextAttemptTimestamp);
    }

    public long getErrNextAttemptTimestamp() {
        return errNextAttemptTimestamp;
    }
    
    public void setSevereErrorNextAttemptTimestamp(){
        totalSevereErrors.incrementAndGet();
        this.errNextAttemptTimestamp = System.currentTimeMillis() + (DistributedConfig.SevereServerErrorAttemptDeltaMillis * rotate(severeErrorsRotate, severeErrorRotateLimit));
    }
    
    public void setNetworkErrorNextAttemptTimestamp(){
        totalNetworkErrors.incrementAndGet();
        this.errNextAttemptTimestamp = System.currentTimeMillis() + (DistributedConfig.NetworkErrorAttemptDeltaMillis * rotate(networkErrorsRotate, networkErrorsRotateLimit));
    }
        
    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isSelf() {
        return self.get();
    }

    void setSelf(boolean self) {
        this.self.set(self);
    }

    public String getServerId() {
        return getServerId(host, port);
    }

    public static String getServerId(String host, int port) {
        if (Utl.areBlank(host)) {
            return null;
        }
        return (host.trim().toLowerCase() + ":" + port);
    }  
}
