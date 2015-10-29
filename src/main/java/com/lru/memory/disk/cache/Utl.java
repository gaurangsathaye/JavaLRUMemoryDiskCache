package com.lru.memory.disk.cache;

import java.security.MessageDigest;

/**
 *
 * @author sathayeg
 */
public class Utl {
    public static boolean areBlank(String... strs) {
        try {
            if ((null == strs) || (strs.length < 1)) {
                return true;
            }
            for (String s : strs) {
                if ((null == s) || s.trim().equals("")) {
                    return true;
                }
            }
        } catch (Exception e) {
            return true;
        }
        return false;
    }
    
    public static String sha256(String key) throws Exception {
        if(areBlank(key)) throw new Exception("key is blank");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes("UTF-8"));
        StringBuilder result = new StringBuilder();
        for (byte byt : digest) {
            String substring = Integer.toString((byt & 0xff) + 0x100, 16).substring(1);
            result.append(substring);           
        }
        return result.toString();
    }
}
