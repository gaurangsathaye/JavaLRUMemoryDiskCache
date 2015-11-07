package com.lru.memory.disk.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author sathayeg
 */
public class Utl {

    private static ExecutorService globalExecutorService = null;
    private static CacheLockManager globalCacheLockManager = null;

    static {
        globalExecutorService = Executors.newFixedThreadPool(15);
        globalCacheLockManager = new CacheLockManager(45);
    }

    static void offerToGlobalExecutorService(Runnable r) {
        try {
            if (null == r) {
                return;
            }
            globalExecutorService.execute(r);
        } catch (Exception e) {
            p("error offering to global executor service");
        }
    }

    static boolean areBlank(String... strs) {
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

    static String sha256(String key) throws Exception {
        if (areBlank(key)) {
            throw new Exception("key is blank");
        }
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes("UTF-8"));
        StringBuilder result = new StringBuilder();
        for (byte byt : digest) {
            String substring = Integer.toString((byt & 0xff) + 0x100, 16).substring(1);
            result.append(substring);
        }
        return result.toString();
    }

    public static void closeAll(AutoCloseable[] arr) {
        if ((null == arr) || (arr.length < 1)) {
            return;
        }
        for (AutoCloseable ac : arr) {
            try {
                if (null != ac) {
                    ac.close();
                }
            } catch (Exception e) {
            }
        }
    }

    static void serializeFile(String pathAndFilename, Serializable obj) {
        ReentrantReadWriteLock.WriteLock writeLock = globalCacheLockManager.getLock(pathAndFilename.hashCode()).writeLock();
        writeLock.lock();
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(pathAndFilename, false);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            p("Utl.serialize : to: " + pathAndFilename);
        } catch (Exception e) {
            p("error: Utl.serialize: " + pathAndFilename + " :: " + e);
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
            try {
                oos.close();
            } catch (Exception e) {
            }
            writeLock.unlock();
        }
    }
    
    static void deleteFile(String pathAndFilename){
        ReentrantReadWriteLock.WriteLock writeLock = globalCacheLockManager.getLock(pathAndFilename.hashCode()).writeLock();
        writeLock.lock();
        try{
            File f = new File(pathAndFilename);
            if(f.exists() && (! f.isDirectory())){
                f.delete();
                p("deleteFile: " + pathAndFilename);
            }
        }catch(Exception e){}
        finally{
            writeLock.unlock();
        }
    }

    static Serializable deserializeFile(String pathAndFilename) {
        ReentrantReadWriteLock.ReadLock readLock = globalCacheLockManager.getLock(pathAndFilename.hashCode()).readLock();
        readLock.lock();
        File f = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            f = new File(pathAndFilename);
            if (!f.exists()) {
                return null;
            }

            fis = new FileInputStream(f);
            ois = new ObjectInputStream(fis);
            Serializable serializable = ((Serializable) (ois.readObject()));
            p("deserializeFile: " + pathAndFilename);
            return serializable;
        } catch (Exception e) {
            p("Utl.deserialize error: " + e);
            //Don't throw any errors here, delete the existing file
            //This may have been due to deserialization incompatibilities from serial version uid or cached item code changes, etc.
            try {
                if(null != f) f.delete();
            } catch (Exception exd) {
            }
            return null;
        } finally {
            try {
                if(null != fis) fis.close();
            } catch (Exception e) {
            }
            try {
                if(null != ois) ois.close();
            } catch (Exception e) {
            }
            readLock.unlock();
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
