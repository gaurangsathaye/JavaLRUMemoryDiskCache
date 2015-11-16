package com.test.lru.memory.disk.cache.server.standalone;

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
    
    static void runTClient1() {
        String request = "line<end> </end> one\n\rline two\n\rline three";
        try{
            tClient1(request);
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void tClient1(String request) throws SocketException, IOException, BadRequestException {
        if(Utl.areBlank(request)) throw new BadRequestException("request is blank", null);
        request = request.replace("end>", "/end>");
        
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
            pw.println(request + "<end>");
            pw.flush();
            
            is = clientSock.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            
            String response = readInput(br);
            p("response: " + response);
            
        } finally {
            Utl.closeAll(closeables);
        }
    }
    
    static String readInput(BufferedReader br) throws IOException {
        String request = null;
        StringBuilder sb = new StringBuilder();
        while(true) {
            request = br.readLine();
            sb.append(request).append("\n");
            if(request.contains("<end>")) break;
        }
        return sb.toString();
    }
    
    static void tReqClean() throws Exception{
        
    }
    
    static void p(Object o){
        System.out.println(o);
    }
}
