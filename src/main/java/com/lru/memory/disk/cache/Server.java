package com.lru.memory.disk.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class Server {

    private static final String Usage = "java com.lru.memory.disk.cache.CacheServer port [serverThreadPoolSize] [diskDataDirectory]";

    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private static String[] tArgs;

    public static void main(String[] args) {
        if ((null == args) || (args.length < 1)) {
            log.error("Invalid arguments, usage: " + Usage);
            System.exit(1);
            return;
        }

        int port = -1;
        try {
            port = Integer.parseInt(args[0]);
            log.info("port: " + port);
        } catch (Exception e) {
            log.error("Invalid port, usage: " + Usage);
            System.exit(1);
            return;
        }

        int serverThreadPoolSize = -1;
        if (args.length > 1) {
            try {
                serverThreadPoolSize = Integer.parseInt(args[1]);
                log.info("server thread pool size: " + serverThreadPoolSize);
            } catch (Exception e) {
                log.error("Invalid server thread pool size, usage: " + Usage);
                System.exit(1);
                return;
            }
        }
        
        String diskDataDirectory = null;
        if(args.length > 2){
            diskDataDirectory = args[2];
            log.info("disk data directory: " + diskDataDirectory);
        }
    }
}
