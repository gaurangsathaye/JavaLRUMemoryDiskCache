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
            proc();
            
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
            is = socket.getInputStream();
            bis = new BufferedInputStream(is);
            isr = new InputStreamReader(bis, "UTF-8");
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
                this.cache.putOnly(key, ce);
               
                response = ServerProtocol.createResponseJson("put success");
                
            }else{
                Map<String, Object> map = this.cache.internalGetOnly(key, true);
                
                boolean fromDisk = (Boolean) map.get(AbstractCacheService.KeyInternalGetFromDisk);
                CacheEntry<String> ce = (CacheEntry<String>) (map.get(AbstractCacheService.KeyInternalGetCachedObj));
                String value = null;
                if( (null != ce) && (! ce.isTtlExpired()) ){
                    //Lazy load disk objects into memory
                    if (fromDisk) {
                        this.cache.internalPutOnly(key, ce, false); //false because we don't want to re-serialize a deserialized object, this is to lazy load to memory
                        
                    }
                    value = ce.getCached();
                }else if( (null != ce) && (ce.isTtlExpired()) ){
                    this.cache.asyncDelete(key); //delete disk items that are expired
                }
                response = ServerProtocol.createResponseJson(value);
                
            }
            
            os = socket.getOutputStream();
            bos = new BufferedOutputStream(os);
            osw = new OutputStreamWriter(bos, "UTF-8");
            bw = new BufferedWriter(osw);
            pw = new PrintWriter(bw, true);

            pw.println(response);
            
            //os.flush();
            //osw.flush();
            //pw.flush();
            
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
