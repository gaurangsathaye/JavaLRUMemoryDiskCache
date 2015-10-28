package com.sg.simple.lru.cache;

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
}
