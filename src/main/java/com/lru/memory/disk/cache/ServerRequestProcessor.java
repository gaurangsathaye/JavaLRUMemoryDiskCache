package com.lru.memory.disk.cache;

import java.net.Socket;

/**
 *
 * @author sathayeg
 */
public class ServerRequestProcessor implements Runnable {

    private final Socket socket;
    private final ServerCache cache;

    ServerRequestProcessor(Socket socket, ServerCache standAloneServerCache) {
        this.socket = socket;
        this.cache = standAloneServerCache;
    }
    
    @Override
    public void run() {
    }
    
}
