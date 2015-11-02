package com.lru.memory.disk.cache.distribute;

import com.lru.memory.disk.cache.Utl;

/**
 *
 * @author sathayeg
 */
public class ClusterServer {
    private final String host;
    private final int port;
    private boolean self = false;
    
    public ClusterServer(String host, String port) throws Exception{
        if(Utl.areBlank(host)) throw new Exception("host is blank");
        this.host = host;
        try{
            this.port = Integer.parseInt(port);
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
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }

    @Override
    public String toString() {
        return "ClusterServer{" + "host=" + host + ", port=" + port + ", self=" + self + '}';
    }   
}
