package com.test.lru.memory.disk.cache.dist;

/**
 *
 * @author sathayeg
 */
public class Tduplicate {
    public static void main(String[] args){
        try{
            t1();
        }catch(Exception e){
            p("error: " + e);
        }
    }
    
    static void t1() throws Exception {
        String[] arr = {"a","b","c", "d", "w", "b", "n", "w"};
        
        for(int i=0;i<arr.length-1;i++){
            int j = i + 1;
            for(;j<arr.length;j++){
                p(arr[i] + " " + arr[j] + " eq? " + arr[i].equals(arr[j]));
            }
        }
    }
    
    static void p(Object o){
        System.out.println(o);
    }
}
