package com.test.lru.memory.disk.cache.server.standalone;

import com.lru.memory.disk.cache.ServerProtocol;
import com.lru.memory.disk.cache.Utl;
import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;

/**
 *
 * @author sathayeg
 */
public class TStandAloneClient {
    public static void main(String[] args){
        try{
            runTClient1();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void runTClient1() throws BadRequestException, IOException {
        String data = 
                //"line<end> </end> one\n\rline two\n\rline three";
                null;
        String putRequestJson = ServerProtocol.createPutRequestJson("key1", data, 1);
        p("putRequestJson: " + putRequestJson);
        
        Map<String, Object> reqMap = ServerProtocol.parseGetPutRequest(putRequestJson);
        p("reqMap: " + reqMap);
        
        try{
            //tClient1(putRequestJson);
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void tClient1(String request) throws SocketException, IOException, BadRequestException {
        if(Utl.areBlank(request)) throw new BadRequestException("request is blank", null);
        
        InputStream is = null; 
        InputStreamReader isr = null;
        BufferedReader br = null;
        
        OutputStream os = null;
        PrintWriter pw = null;

        Socket clientSock = null;

        AutoCloseable closeables[] = {is, os, clientSock};
        try {            
            //clientSock = new Socket(clusterServerForCacheKey.getHost(), clusterServerForCacheKey.getPort());
            clientSock = new Socket();
            clientSock.connect(new InetSocketAddress("127.0.0.1", 23290), 5000);
            clientSock.setSoTimeout(10000);
            
            os = clientSock.getOutputStream();
            pw = new PrintWriter(os, true);
            pw.println(request);
            pw.flush();
            
            is = clientSock.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            
            String response = br.readLine();
            p("response: " + response);
            
        } finally {
            Utl.closeAll(closeables);
        }
    }  
    
    static void p(Object o){
        System.out.println(o);
    }
}
