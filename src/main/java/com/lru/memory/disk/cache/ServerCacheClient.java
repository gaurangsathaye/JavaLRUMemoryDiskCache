package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class ServerCacheClient {
    
    private static final Logger log = LoggerFactory.getLogger(ServerCacheClient.class);
    
    private static final int RemoteAttempts = 2;

    private final String clusterConfig;
    private final int clientConnTimeoutMillis;
    private final int clientReadTimeoutMillis;
    private final DistributedManager distMgr;
    
    public ServerCacheClient(String clusterConfig, int clientConnTimeoutMillis, int clientReadTimeoutMillis) throws Exception{
        this.clusterConfig = clusterConfig;
        this.clientConnTimeoutMillis = clientConnTimeoutMillis;
        this.clientReadTimeoutMillis = clientReadTimeoutMillis;
        
        //first arg for DistributedConfig serverThreadPoolSize doesn't matter for client, so just set to 1
        this.distMgr = DistributedManager.getDistributedManagerForStandaloneClient(clusterConfig, 
                new DistributedConfig(1, clientConnTimeoutMillis, clientReadTimeoutMillis, false));
    }
    
    public String get(String key) throws BadRequestException, IOException, Exception{
        Exception ex = null;
        for(int i=0;i<RemoteAttempts;i++){
            DistributedConfigServer remoteServer = this.getServerForKey(key);
            try{
                return remoteCall(remoteServer.getHost(), remoteServer.getPort(), ServerProtocol.createGetRequestJson(key));
            }catch(BadRequestException | IOException e){
                log.error("Unable to get data from: " + remoteServer.getServerId(), e);
                remoteServer.setNetworkErrorNextAttemptTimestamp();
                ex = e;
            }
        }
        throw ex;
    }
    
    public String put(String key, String value, long ttlMillis) throws BadRequestException, IOException, Exception {       
        Exception ex = null;
        for(int i=0;i<RemoteAttempts;i++){
            DistributedConfigServer remoteServer = this.getServerForKey(key);
            try{
                return remoteCall(remoteServer.getHost(), remoteServer.getPort(), ServerProtocol.createPutRequestJson(key, value, ttlMillis));
            }catch(BadRequestException | IOException e){
                log.error("Unable to put data to: " + remoteServer.getServerId(), e);
                remoteServer.setNetworkErrorNextAttemptTimestamp();
                ex = e;
            }
        }
        throw ex;
    }
    
    private DistributedConfigServer getServerForKey(String key) throws BadRequestException{
        return this.distMgr.getClusterServerForCacheKeyRotate(key);
    }
    
    private String remoteCall(String host, int port, String request) throws SocketException, IOException, BadRequestException {
        if(Utl.areBlank(host, request)) throw new BadRequestException("Host or request is blank", null);
        
        InputStream is = null; 
        InputStreamReader isr = null;
        BufferedReader br = null;
        
        OutputStream os = null;
        OutputStreamWriter osw = null;
        PrintWriter pw = null;

        Socket clientSock = null;

        AutoCloseable closeables[] = {is, isr, br, os, osw, pw, clientSock};
        try {            
            //clientSock = new Socket(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort());
            clientSock = new Socket();
            clientSock.connect(new InetSocketAddress(host, port), this.clientConnTimeoutMillis);
            clientSock.setSoTimeout(this.clientReadTimeoutMillis);
            
            os = clientSock.getOutputStream();
            osw = new OutputStreamWriter(os, "UTF-8");
            pw = new PrintWriter(osw, true);
            pw.println(request);
            os.flush();
            osw.flush();
            pw.flush();
            
            is = clientSock.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            
            return br.readLine();
        } finally {
            Utl.closeAll(closeables);
        }
    } 
}
