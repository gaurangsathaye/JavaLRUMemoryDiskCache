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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author sathayeg
 */
public class TStandAloneClient {

    public static void main(String[] args) {
        try {
            //runTClient1();

            //tRequests();
            //tServerCacheClient();
            new TStandAloneClient().tServerCacheClientThreads();
        } catch (Exception e) {
            p("error: " + e);
        }
    }

    void tServerCacheClientThreads() throws Exception {
        String clusterConfig = "127.0.0.1:23290, 127.0.0.1:23291";
        ServerCacheClient client = new ServerCacheClient(clusterConfig, 2000, 3000);

        ExecutorService execService = Executors.newFixedThreadPool(50);
        long ct = 1L;
        while (true) {
            execService.execute(new ClientThread(client, ct));
            ++ct;
        }
    }

    private class ClientThread implements Runnable {

        private final ServerCacheClient client;
        long i = 0;
        long j = 0;

        ClientThread(ServerCacheClient client, long ct) {
            this.client = client;
            i = ct % 2;
            j = ct % 100;
        }

        @Override
        public void run() {
            try {
                long start = System.currentTimeMillis();
                intrun();
                long time = (System.currentTimeMillis() - start);
                if (time > 3000) {
                    p("time: " + time);
                }
            } catch (Exception e) {
                p("error client thread: " + e);
            }
        }

        void intrun() {
            long start = System.currentTimeMillis();
            Random rnd = new Random();
            long time = (System.currentTimeMillis() - start);
            if (time > 200) {
                p("new random time: " + time);
            }
            try {
                start = System.currentTimeMillis();
                long sleeptime = rnd.nextInt(1000);
                //Thread.sleep(sleeptime);
                while ((System.currentTimeMillis() - start) < sleeptime) {
                }
                //time = System.currentTimeMillis() - start;
                if (time > 1100) {
                    p("sleep time: " + time + ", sleeptime: " + sleeptime);
                }
            } catch (Exception e) {
            }

            try {
                if (i == 0) {
                    start = System.currentTimeMillis();
                    p(client.put("key" + j, "value" + j, 10000));
                    time = System.currentTimeMillis() - start;
                    if (time > 2000) {
                        p("put time: " + time);
                    }
                } else if (i == 1) {
                    start = System.currentTimeMillis();
                    p(client.get("key" + j));
                    time = System.currentTimeMillis() - start;
                    if (time > 2000) {
                        p("get time: " + time);
                    }
                } else {
                    p("invalid i: " + i);
                    System.exit(0);
                }
            } catch (Exception e) {
                p("error: " + e);
            }
        }

    }

    static void tServerCacheClient() throws Exception {
        String clusterConfig = "127.0.0.1:23290, 127.0.0.1:23291";
        ServerCacheClient client = new ServerCacheClient(clusterConfig, 5000, 10000);

        for (int i = 0; i < 2; i++) {
            p(client.put("key" + i, "value" + i, 600000));
        }

        for (int i = 0; i < 2; i++) {
            p(client.get("key" + i));
        }

        for (int i = 0; i < 2; i++) {
            p(client.get("key" + i));
        }

        try {
            Thread.sleep(25000);
        } catch (Exception e) {
        }

        for (int i = 0; i < 2; i++) {
            p(client.get("key" + i));
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
