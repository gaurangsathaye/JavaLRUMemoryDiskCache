package com.lru.memory.disk.cache;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.lru.memory.disk.cache.exceptions.BadRequestException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sathayeg
 */
public class ServerProtocol {
    
    public static final String KeyKey = "k";
    public static final String KeyValue = "v";
    public static final String KeyTtlMillis = "t";
    
    /**
     * 
     * @param key
     * @param value
     * @param ttlMillis
     * @return
     * @throws BadRequestException
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public static String createPutRequestJson(String key, String value, long ttlMillis) throws BadRequestException, UnsupportedEncodingException, IOException{
        if(ttlMillis < 1) throw new BadRequestException("Invalid ttl, ttl must be greater than 0.", null);
        if(Utl.areBlank(key)) throw new BadRequestException("Key is blank.", null);
        
        ByteArrayOutputStream baos = null;
        JsonGenerator jsongen = null;
        try {
            baos = new ByteArrayOutputStream();
            JsonFactory jfactory = Utl.jsonFactory;
            jsongen = jfactory.createGenerator(baos);
            jsongen.writeStartObject();
            jsongen.writeNumberField(KeyTtlMillis, ttlMillis);
            jsongen.writeStringField(KeyKey, new String(key.getBytes("UTF-8")));
            if(null == value){
                jsongen.writeNullField(KeyValue);
            }else{
                jsongen.writeStringField(KeyValue, new String(value.getBytes("UTF-8")));
            }
            jsongen.writeEndObject();
            jsongen.close();
            jsongen = null;
            
            return new String(baos.toByteArray(), "UTF-8");           
        } finally {
            try{if(null != baos) baos.close();}catch(Exception e){}
            try{if(null != jsongen) jsongen.close();}catch(Exception e){}
        }
    }
    
    /**
     * 
     * @param key
     * @return
     * @throws BadRequestException
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public static String createGetRequestJson(String key) throws BadRequestException, UnsupportedEncodingException, IOException {
        if(Utl.areBlank(key)) throw new BadRequestException("Key is blank.", null);
        
        ByteArrayOutputStream baos = null;
        JsonGenerator jsongen = null;
        try {
            baos = new ByteArrayOutputStream();
            JsonFactory jfactory = Utl.jsonFactory;
            jsongen = jfactory.createGenerator(baos);
            jsongen.writeStartObject();
            jsongen.writeStringField(KeyKey, new String(key.getBytes("UTF-8")));           
            jsongen.writeEndObject();
            jsongen.close();
            jsongen = null;
            
            return new String(baos.toByteArray(), "UTF-8");           
        } finally {
            try{if(null != baos) baos.close();}catch(Exception e){}
            try{if(null != jsongen) jsongen.close();}catch(Exception e){}
        }
    }
    
    /**
     * 
     * @param value
     * @return
     * @throws BadRequestException
     * @throws UnsupportedEncodingException
     * @throws IOException 
     */
    public static String createResponseJson(String value) throws BadRequestException, UnsupportedEncodingException, IOException {      
        ByteArrayOutputStream baos = null;
        JsonGenerator jsongen = null;
        try {
            baos = new ByteArrayOutputStream();
            JsonFactory jfactory = Utl.jsonFactory;
            jsongen = jfactory.createGenerator(baos);
            jsongen.writeStartObject();
            if(null == value){
                jsongen.writeNullField(KeyValue);
            }else{
                jsongen.writeStringField(KeyValue, new String(value.getBytes("UTF-8")));
            }
            jsongen.writeEndObject();
            jsongen.close();
            jsongen = null;
            
            return new String(baos.toByteArray(), "UTF-8");           
        } finally {
            try{if(null != baos) baos.close();}catch(Exception e){}
            try{if(null != jsongen) jsongen.close();}catch(Exception e){}
        }
    }
    
    public static Map<String, Object> parseRequestResponse(String json) throws BadRequestException, IOException{
        if(Utl.areBlank(json)) throw new BadRequestException("Get/Put  is blank.", null);
       
        JsonFactory jfactory = Utl.jsonFactory;
        JsonParser parser = jfactory.createParser(json.getBytes("UTF-8"));
        
        Map<String, Object> map = new HashMap<>();
        while(parser.nextToken() != JsonToken.END_OBJECT){
            String field = parser.getCurrentName();
            if(null == field) continue;
            parser.nextToken();
            if(field.equals(KeyKey)){
                String key = parser.getText();
                map.put(KeyKey, key);
            }else if(field.equals(KeyTtlMillis)){
                long ttl = parser.getLongValue();
                map.put(KeyTtlMillis, ttl);
            }else if(field.equals(KeyValue)){
                String value = 
                        //parser.getText();
                        parser.getValueAsString();
                map.put(KeyValue, value);
            }else{
                throw new BadRequestException("Invalid Get/Put request json", null);
            }
        }    
        parser.close();
        return map;
    }
    
}
