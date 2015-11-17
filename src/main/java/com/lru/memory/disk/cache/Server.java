package com.lru.memory.disk.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class Server {

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private static final String Usage = "java -Dport=23290 -Dcache.size=50000 -Ddisk.cache.dir=\"./standalone/cache\" -Dserver.threads=200 com.lru.memory.disk.cache.Server";

    public static final String CacheName = "stand_alone_cache";

    private static int port = 23290;
    private static int serverThreads = 200;
    private static int cacheSize = 50000;
    private static String diskCacheDir;

    private static DistributedConfig distConfig;
    private static ServerCache cache;
    private static DistributedManager distMgr;

    public static void main(String[] args) throws Exception {
        
        getSetupInfo();
        
        distConfig = new DistributedConfig(serverThreads,
                DistributedConfig.getDefaultClientConnectTimeoutMillis(),
                DistributedConfig.getDefaultClientReadTimeoutMillis(),
                DistributedConfig.isDefaultCacheDistributedResponse());

        if (null == diskCacheDir) {
            log.info("Created memory only cache.");
            cache = new ServerCache(CacheName, cacheSize);
        } else {
            log.info("Created memory and disk cache.");
            cache = new ServerCache(CacheName, cacheSize, true, diskCacheDir);
        }
        
        distMgr = DistributedManager.getDistributedManagerForStandalone(port, distConfig, cache);
        distMgr.startServer();
    } //end main

    static void getSetupInfo() {
        try {
            port = Integer.parseInt(System.getProperty("port", Integer.toString(port)));
            log.info("Port: " + port);
        } catch (Exception e) {
            log.error("Invalid port, usage: " + Usage);
            System.exit(1);
            return;
        }

        try {
            serverThreads = Integer.parseInt(System.getProperty("server.threads", Integer.toString(serverThreads)));
            log.info("Server threads: " + serverThreads);
        } catch (Exception e) {
            log.error("Invalid server.threads, usage: " + Usage);
            System.exit(1);
            return;
        }

        try {
            cacheSize = Integer.parseInt(System.getProperty("cache.size", Integer.toString(cacheSize)));
            log.info("Cache size: " + cacheSize);
        } catch (Exception e) {
            log.error("Invalid cache.size, usage: " + Usage);
            System.exit(1);
            return;
        }

        diskCacheDir = System.getProperty("disk.cache.dir");
        if (diskCacheDir != null) {
            diskCacheDir = diskCacheDir.trim();
            if (Utl.areBlank(diskCacheDir)) {
                diskCacheDir = null;
            } else {
                log.info("disk cache directory: " + diskCacheDir);
            }
        }
    }

} //end public class Server
