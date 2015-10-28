package com.example.sg.simple.lru;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *
 * @author sathayeg
 */
public class ExampleObjectToCache implements Serializable{
    private static final long serialVersionUID = 1L;
    
    private long lastModfied = 0;
    private String data;
    private final String id;
    
    public ExampleObjectToCache(String id){
        this.id = id;
    }
    
    /*
        You choose how to determine if your object is valid in the cache.
    
        Here as an example, we are saying that if last modified is less than
        20 seconds from the current time, it is valid, otherwise it is not.
    */
    public boolean isValid(){
        return ((System.currentTimeMillis() - lastModfied) < 20000);
    }

    public long getLastModfied() {
        return lastModfied;
    }

    public void setLastModfied(long lastModfied) {
        this.lastModfied = lastModfied;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ExampleMyObjectToCache{" + "lastModfied=" + lastModfied + ", data=" + data + ", id=" + id + '}';
    }
    
    public static void main(String[] args){
        ExampleObjectToCache obj = new ExampleDao().get("test2");
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try{
            fos = new FileOutputStream("test", false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
        }catch(Exception e){
            p("error: " + e);
        }finally{
            try{fos.close();}catch(Exception e){}
            try{oos.close();}catch(Exception e){}
        }
    }
    
    static void p(Object o){
        System.out.println(o);
    }
    
}
