package com.lru.memory.disk.cache;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author sathayeg
 */
 public class DistributedConfigServer {
    private final String host;
    private final int port;
    private AtomicBoolean self = new AtomicBoolean(false);
    
    public DistributedConfigServer(String host, String port) throws Exception{
        if(Utl.areBlank(host, port)) throw new Exception("host and/or port is blank");
        this.host = host.trim().toLowerCase();
        try{
            this.port = Integer.parseInt(port.trim());
        }catch(Exception e){
            throw new Exception("Invalid cluster port: " + port);
        }
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
        return "ClusterServer{" + "host=" + host + ", port=" + port + ", self=" + self.get() + '}';
    }   
}
