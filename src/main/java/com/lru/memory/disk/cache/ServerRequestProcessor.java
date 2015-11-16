package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sathayeg
 */
public class ServerRequestProcessor implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServerRequestProcessor.class);

    private final Socket socket;
    private final ServerCache cache;

    ServerRequestProcessor(Socket socket, ServerCache standAloneServerCache) {
        this.socket = socket;
        this.cache = standAloneServerCache;
    }

    @Override
    public void run() {
        try {
            proc();
        } catch (Exception e) {
            log.error("Error processing", e);
        }
    }

    private void proc() {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        OutputStream os = null;
        OutputStreamWriter osw = null;
        PrintWriter pw = null;

        AutoCloseable closeables[] = {is, isr, br, os, osw, pw, socket};

        try {
            is = socket.getInputStream();
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);

            String request = br.readLine();
            Map<String, Object> reqMap = ServerProtocol.parseRequestResponse(request);
            boolean put = false;
            if(reqMap.containsKey(ServerProtocol.KeyKey) && reqMap.containsKey(ServerProtocol.KeyTtlMillis) && reqMap.containsKey(ServerProtocol.KeyValue)){
                put = true;
            }else if(reqMap.containsKey(ServerProtocol.KeyKey)){
                put = false;
            }else{
                throw new BadRequestException("Invalid request: " + request, null);
            }
            
            String response = null;
            if(put){
                String value = null;
                Object valueObj= reqMap.get(ServerProtocol.KeyValue);
                if(null != valueObj) value = (String) valueObj;
                
                long ttl = (long) reqMap.get(ServerProtocol.KeyTtlMillis);
                String key = (String) reqMap.get(ServerProtocol.KeyKey);
                CacheEntry<String> ce = new CacheEntry<>(value, System.currentTimeMillis());
                ce.setTtl(ttl);
                this.cache.putOnly(key, ce);
                response = ServerProtocol.createResponseJson("put success");
            }else{
                String key = (String) reqMap.get(ServerProtocol.KeyKey);
                CacheEntry<String> ce = this.cache.getOnly(key);
                String value = null;
                if( (null != ce) && (! ce.isTtlExpired()) ){
                    value = ce.getCached();
                }
                response = ServerProtocol.createResponseJson(value);
            }

            os = socket.getOutputStream();
            osw = new OutputStreamWriter(os, "UTF-8");
            pw = new PrintWriter(osw, true);

            pw.println(response);
            
            os.flush();
            osw.flush();
            pw.flush();
        } catch (Exception e) {
            log.error("Unable to process request", e);
        } finally {
            Utl.closeAll(closeables);
        }
    }
    
    /*private String processRequest(String req) throws BadRequestException{
        if(Utl.areBlank(req)) throw new BadRequestException("request is blank", null);
    }*/
}
