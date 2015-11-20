package com.test.lru.memory.disk.cache.server.standalone;

import com.lru.memory.disk.cache.DistributedConfigServer;
import com.lru.memory.disk.cache.ServerCacheClient;
import com.lru.memory.disk.cache.ServerProtocol;
import com.lru.memory.disk.cache.Utl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sathayeg
 */
public class TStandAloneClient {

    public static void main(String[] args) {
        try {
            TStandAloneClient tsc = new TStandAloneClient();
            
            //runTClient1();

            //tRequests();
            tsc.tServerCacheClient();
        } catch (Exception e) {
            p("error: " + e);
        }
    }    

    void tServerCacheClient() throws Exception {
        String clusterConfig = "127.0.0.1:23290, 127.0.0.1:23291";
        ServerCacheClient client = new ServerCacheClient(clusterConfig, 1000, 2000);
        
        Random rnd = new Random();
        
        ExecutorService es = Executors.newFixedThreadPool(15);
        List<Future<String>> futures = new ArrayList<>();
        
        for(int i=0;i<10000;i++){
            //String put = client.put("key"+rnd.nextInt(20), "value"+rnd.nextInt(100), 10000);
            //String get = client.get("key"+rnd.nextInt(20));
            //p(put);
            //p(get);
            //new Thread(new ClientThread(client, rnd)).start();
            //es.execute(new ClientThread(client, rnd));
            Future<String> submit = es.submit(new ClientThread(client, rnd));
            futures.add(submit);
            
            /*
            long sleepTime = (long) (rnd.nextInt(10));
            //p("sleepTime: " + sleepTime);
            long start = System.currentTimeMillis();
            try{Thread.sleep(sleepTime);}catch(Exception e){}
            long time = System.currentTimeMillis() - start;
            //p("sleepTime: " + sleepTime + ", actual sleep time: " + time);
            */
        }
        
        int i = 0;
        for(Future<String> f : futures){
            f.get();
            ++i;
        }
        p("got futures: " + i);
        System.exit(0);
    }
    
    class ClientThread implements Callable<String>{

        private final ServerCacheClient client;
        private final Random rnd;
        
        ClientThread(ServerCacheClient client, Random rnd){
            this.client = client;
            this.rnd = rnd;
        }

        /*@Override
        public void run() {
            try{
                String put = client.put("key"+rnd.nextInt(20), "value"+rnd.nextInt(100), 10000);
                String get = client.get("key"+rnd.nextInt(20));
                p("es 1: " + put);
                p("es 1: " + get);
            }catch(Exception e){
                p("client thead error: " + e);
            }
        }   */     

        @Override
        public String call() throws Exception {
            try{
                long sleepTime = (long) (rnd.nextInt(100));
                try{Thread.sleep(sleepTime);}catch(Exception e){}
                String put = client.put("key"+rnd.nextInt(200), "value"+rnd.nextInt(100), 10000);
                String get = client.get("key"+rnd.nextInt(200));
                p("es 1: " + put);
                p("es 1: " + get);
            }catch(Exception e){
                p("client thead error: " + e);
            }
            return "r";
        }
    }

    static void tRequests() throws Exception {
        String data
                = "lineone\n\rline two\n\rline three";
        //null;
        String putRequestJson = ServerProtocol.createPutRequestJson("key1", data, Long.MAX_VALUE);
        p("putRequestJson: " + putRequestJson);
        Map<String, Object> map = ServerProtocol.parseRequestResponse(putRequestJson);
        p("put request map: " + map + "\n");

        String getRequestJson = ServerProtocol.createGetRequestJson("key1");
        p("getRequestJson: " + getRequestJson);
        map = ServerProtocol.parseRequestResponse(getRequestJson);
        p("get request map: " + map + "\n");

        String responseJson = ServerProtocol.createResponseJson(data);
        p("responseJson: " + responseJson);
        map = ServerProtocol.parseRequestResponse(responseJson);
        p("response json map: " + map + "\n");

        String badRequestJson = "{\"a\":\"b\", \"k\":\"k1\"}";
        p("bad request json: " + badRequestJson);
        map = ServerProtocol.parseRequestResponse(badRequestJson);
    }

    static void runTClient1() throws BadRequestException, IOException {
        String badRequestJson = "{\"a\":\"b\", \"k\":\"k1\"}";

        try {
            //tClient1(ServerProtocol.createPutRequestJson("key1", "value1 Ã ϡ", 120000));

            tClient1(ServerProtocol.createGetRequestJson("key1"));

            //try{Thread.sleep(2500);}catch(Exception e){}
            //tClient1(ServerProtocol.createGetRequestJson("key1"));
            //tClient1(badRequestJson);
        } catch (Exception e) {
            p("error: " + e);
        }
    }

    static void tClient1(String request) throws SocketException, IOException, BadRequestException {
        if (Utl.areBlank(request)) {
            throw new BadRequestException("request is blank", null);
        }

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
            clientSock.connect(new InetSocketAddress("127.0.0.1", 23290), 5000);
            clientSock.setSoTimeout(10000);

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

            String response = br.readLine();
            p("response: " + response);
        } finally {
            Utl.closeAll(closeables);
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
