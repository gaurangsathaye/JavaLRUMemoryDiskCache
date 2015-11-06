package com.lru.memory.disk.cache;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author sathayeg
 */
public class DistributedConfigServer {
    
    public static final long SevereServerErrorAttemptDelta = 5000L;
    
    private final String host;
    private final int port;
    private AtomicBoolean self = new AtomicBoolean(false);
    private long errNextAttemptTimestamp = 0L;

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

    public boolean tryRemote() {
        return (System.currentTimeMillis() > errNextAttemptTimestamp);
    }

    public long getErrNextAttemptTimestamp() {
        return errNextAttemptTimestamp;
    }
    
    public void setSevereErrorNextAttemptTimestamp(){
        this.errNextAttemptTimestamp = System.currentTimeMillis() + SevereServerErrorAttemptDelta;
    }

    public void setErrNextAttemptTimestamp(long errNextAttemptTimestamp) {
        this.errNextAttemptTimestamp = errNextAttemptTimestamp;
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

    @Override
    public String toString() {
        return getServerId(host, port);
    }

    public static String getServerId(String host, int port) {
        if (Utl.areBlank(host)) {
            return null;
        }
        return (host.trim().toLowerCase() + ":" + port);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DistributedConfigServer other = (DistributedConfigServer) obj;
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (!Objects.equals(this.self, other.self)) {
            return false;
        }
        return true;
    }
}
