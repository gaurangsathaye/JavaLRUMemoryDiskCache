package com.lru.memory.disk.cache;

import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
            long start = System.currentTimeMillis();
            proc();
            long time = (System.currentTimeMillis() - start);
            if(time > 2000){
                log.info("time: " + time);
            }
        } catch (Exception e) {
            log.error("Error processing", e);
        }
    }

    private void proc() {
        InputStream is = null;
        BufferedInputStream bis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        OutputStream os = null;
        BufferedOutputStream bos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        PrintWriter pw = null;

        AutoCloseable closeables[] = {is, bis, isr, br, os, bos, osw, bw, pw, socket};

        try {
            long start = System.currentTimeMillis();
            is = socket.getInputStream();
            bis = new BufferedInputStream(is);
            isr = new InputStreamReader(bis, "UTF-8");
            br = new BufferedReader(isr);

            String request = br.readLine();
            long time = System.currentTimeMillis() - start;
            if(time > 1000){
                log.info("socket read time: " + time);
            }
            start = System.currentTimeMillis();
            Map<String, Object> reqMap = ServerProtocol.parseRequestResponse(request);
            time = System.currentTimeMillis() - start;
            if(time > 1000){
                log.info("parse request response time: " + time);
            }
            boolean put = false;
            if(reqMap.containsKey(ServerProtocol.KeyKey) && reqMap.containsKey(ServerProtocol.KeyTtlMillis) && reqMap.containsKey(ServerProtocol.KeyValue)){
                put = true;
            }else if(reqMap.containsKey(ServerProtocol.KeyKey)){
                put = false;
            }else{
                throw new BadRequestException("Invalid request: " + request, null);
            }
            
            String key = (String) reqMap.get(ServerProtocol.KeyKey);
            if(Utl.areBlank(key)) throw new BadRequestException("key is blank", null);
            
            String response = null;
            if(put){
                String value = null;
                Object valueObj= reqMap.get(ServerProtocol.KeyValue);
                if(null != valueObj) value = (String) valueObj;
                long ttl = (long) reqMap.get(ServerProtocol.KeyTtlMillis);
                CacheEntry<String> ce = new CacheEntry<>(value, System.currentTimeMillis());
                ce.setTtl(ttl);
                start = System.currentTimeMillis();
                this.cache.putOnly(key, ce);
                time = System.currentTimeMillis() - start;
                if(time > 1000){
                    log.info("put only time: " + time);
                }
                start = System.currentTimeMillis();
                response = ServerProtocol.createResponseJson("put success");
                time = System.currentTimeMillis() - start;
                if(time > 1000){
                    log.info("create response json time: " + time);
                }
            }else{
                start = System.currentTimeMillis();
                Map<String, Object> map = this.cache.internalGetOnly(key, true);
                time = System.currentTimeMillis() - start;
                if(time > 1000){
                    log.info("cache internal get only time: " + time);
                }
                boolean fromDisk = (Boolean) map.get(AbstractCacheService.KeyInternalGetFromDisk);
                CacheEntry<String> ce = (CacheEntry<String>) (map.get(AbstractCacheService.KeyInternalGetCachedObj));
                String value = null;
                if( (null != ce) && (! ce.isTtlExpired()) ){
                    //Lazy load disk objects into memory
                    if (fromDisk) {
                        start = System.currentTimeMillis();
                        this.cache.internalPutOnly(key, ce, false); //false because we don't want to re-serialize a deserialized object, this is to lazy load to memory
                        time = System.currentTimeMillis() - start;
                        if(time > 1000){
                            log.info("internal put only time: " + time);
                        }
                    }
                    value = ce.getCached();
                }else if( (null != ce) && (ce.isTtlExpired()) ){
                    this.cache.asyncDelete(key); //delete disk items that are expired
                }
                start = System.currentTimeMillis();
                response = ServerProtocol.createResponseJson(value);
                time = System.currentTimeMillis() - start;
                if(time > 1000){
                    log.info("create response json time: " + time);
                }
            }
            
            start = System.currentTimeMillis();
            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);
            osw = new OutputStreamWriter(bos, "UTF-8");
            bw = new BufferedWriter(osw);
            pw = new PrintWriter(bw, true);

            log.info("response: " + response);
            pw.println(response);
            
            //os.flush();
            //osw.flush();
            //pw.flush();
            time = System.currentTimeMillis() - start;
            if(time > 1000){
                log.info("server socket out print response time: " + time);
            }
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
