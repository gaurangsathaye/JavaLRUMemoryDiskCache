package com.test.lru.memory.disk.cache.server.standalone;

import com.lru.memory.disk.cache.DistributedRequestResponse;
import com.lru.memory.disk.cache.Utl;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
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
            tClient1();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void tClient1() throws SocketException, IOException {
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
            pw.println("line 1\nline 2");
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
