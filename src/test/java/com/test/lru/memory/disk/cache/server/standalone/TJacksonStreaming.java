package com.test.lru.memory.disk.cache.server.standalone;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lru.memory.disk.cache.ServerProtocol;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sathayeg
 */
public class TJacksonStreaming {

    public static void main(String[] args) {
        try {
            //createJson();
            parse();
        } catch (Exception e) {
            p("error: " + e);
        }
    }
    
    static void parse() throws Exception {
        String json = "{\"v\":null}";
        
        Map<String, Object> map = ServerProtocol.parseRequestResponse(json);
        p(map);
        if(null == map.get("v")){
            p("v is null");
        }else{
            p("v is not null");
        }
    }

    static void createJson() throws Exception {
        ObjectMapper objmapper = new ObjectMapper();
        
        Map map = new HashMap();
        map.put("id", "id1");
        map.put("data", "data line <end> one\ndata <end> line two\nline three.");
        
        String incJson = 
                objmapper.writeValueAsString(map);
                //"data line <end> one\ndata <end> line two\nline three.";
        p("incJson: " + incJson);
        
        String putReqJson = ServerProtocol.createPutRequestJson("key1", incJson, 2000);
        p("putReqJson: " + putReqJson);
        
        Map resmap = objmapper.readValue(putReqJson, Map.class);
        String value = (String) resmap.get("v");
        p("value: " + value);
        
        Map datamap = objmapper.readValue(value, Map.class);
        String data = (String) datamap.get("data");
        p("data: " + data);
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
