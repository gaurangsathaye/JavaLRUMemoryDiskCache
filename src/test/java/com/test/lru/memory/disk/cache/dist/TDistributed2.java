package com.test.lru.memory.disk.cache.dist;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.Distributor;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sathayeg
 */
public class TDistributed2 {

    public static void main(String[] args) {
        try{
            TDistributed2 td = new TDistributed2();
            td.distribute();
        }catch(Exception e){
            p("err: " + e + " : cause: " + e.getCause());
            System.exit(0);
        }
    }
   
    void distribute() throws Exception {
        p("start distribute");
        Cache cache1 = new Cache("teaCache", 10, new Dao("server1"));
        Cache cache2 = new Cache("teaCache", 10, new Dao("server2"));
        Cache cache3 = new Cache("coffeeCache", 10, new Dao("server1"));
        Cache cache4 = new Cache("coffeeCache", 10, new Dao("server2"));
        
        List<Cache> cacheList = new ArrayList<>();
        cacheList.add(cache1);
        cacheList.add(cache2);
        cacheList.add(cache3);
        cacheList.add(cache4);
        
        String clusterConfig = "127.0.0.1:19000, 127.0.0.1:19001";
        
        Distributor.distribute(19000, clusterConfig, cache1, cache3);
        Distributor.distribute(19001, clusterConfig, cache2, cache4);
        
        for(int a=0;a<3;a++){
            for(Cache cache : cacheList){
                for(int i=0;i<2;i++){
                    String val1 = cache.get("key1");
                    p("val1: " + val1);
                    p("-----" ); p(" ");
                    String val2 = cache.get("key2");
                    p("val2: " + val2);
                    p("-----" ); p(" ");
                }
                p("\n===============\n" );
            }
            if(1 == a){
                p("sleep to get past network errors");
                try{Thread.sleep(2200);}catch(Exception e){}
            }
            p("\n" + a + " : *********************\n");
        }
        
        System.exit(0);
    }   
    
    public class Cache extends AbstractCacheService<String> {
        private final Dao dao;

        public Cache(String cacheName, int cacheSize, Dao dao) throws Exception {
            super(cacheName, cacheSize);
            this.dao = dao;
        }

        @Override
        public boolean isCacheItemValid(String o) {
            return (null != o);
        }

        @Override
        public String loadData(String key) throws Exception {
            return this.dao.getData(key, this.getCacheName());
        }
        
    }
    
    class Dao {
        private final String server;
        public Dao(String server){
            this.server = server;
        }
        
        public String getData(String key, String cacheName){
            return "data for key: " + key + ", cacheName: " + cacheName + ", server: " + server;
        }
    }
    
    static void p(Object o) {
        System.out.println(o);
    }
}
