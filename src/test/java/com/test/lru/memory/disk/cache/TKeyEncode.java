package com.test.lru.memory.disk.cache;

import java.security.MessageDigest;

/**
 *
 * @author sathayeg
 */
public class TKeyEncode {
    public static void main(String[] args){
        try{
            tEnc1();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void tEnc1() throws Exception {
        String key = "a9";
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(key.getBytes("UTF-8"));
        StringBuilder result = new StringBuilder();
        for (byte byt : digest) {
            String substring = Integer.toString((byt & 0xff) + 0x100, 16).substring(1);
            result.append(substring);           
        }
        p(result.toString());
    }
    
    static void p(Object o){
        System.out.println(o);
    }
}
