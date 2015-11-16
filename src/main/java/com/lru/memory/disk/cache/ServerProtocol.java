package com.lru.memory.disk.cache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author sathayeg
 */
public class ServerProtocol {
    public static String createPutRequestJson(String key, String value, long ttl) throws BadRequestException, UnsupportedEncodingException, IOException{
        if(ttl < 1) throw new BadRequestException("Invalid ttl, ttl must be greater than 1.", null);
        if(Utl.areBlank(key, value)) throw new BadRequestException("Key or value is blank.", null);
        
        ByteArrayOutputStream baos = null;
        JsonGenerator jsongen = null;
        try {
            baos = new ByteArrayOutputStream();
            JsonFactory jfactory = new JsonFactory();
            jsongen = jfactory.createGenerator(baos);
            jsongen.writeStartObject();
            jsongen.writeStringField("a", "put");
            jsongen.writeNumberField("t", ttl);
            jsongen.writeStringField("k", key);
            jsongen.writeStringField("v", value);
            jsongen.writeEndObject();
            jsongen.close();
            jsongen = null;
            
            String putreq = new String(baos.toByteArray(), "UTF-8");
            putreq = putreq.replace("end>", "/end>");
            return (putreq + "<end>");
        } finally {
            try{if(null != baos) baos.close();}catch(Exception e){}
            try{if(null != jsongen) jsongen.close();}catch(Exception e){}
        }
    }
}
