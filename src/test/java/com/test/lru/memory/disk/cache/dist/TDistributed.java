package com.test.lru.memory.disk.cache.dist;

import com.lru.memory.disk.cache.AbstractCacheService;
import com.lru.memory.disk.cache.distribute.DistributedManager;
import com.lru.memory.disk.cache.distribute.Distributor;

/**
 *
 * @author sathayeg
 */
public class TDistributed {

    public static void main(String[] args) {
        try{
            TDistributed td = new TDistributed();
            //td.createMgr();
            td.distribute();
        }catch(Exception e){
            p("err: " + e + " : cause: " + e.getCause());
        }
    }

    void createMgr() throws Exception {
        Cache cache = new Cache("mycache", 10);
        DistributedManager dm = new DistributedManager(9090, "127.0.0.1:9090", cache);
        p("dm cache map size: " + dm.getCacheMap().size());
        p("dm cluster servers size: " + dm.getClusterServers().size());
        p("dm server port: " + dm.getServerPort());
    }
    
    void distribute() throws Exception {
        p("start distribute");
        Cache cache = new Cache("mycache", 10);
        Distributor.distribute(18080, "127.0.0.1:18080", cache);
    }
    
    public class Cache extends AbstractCacheService<String> {

        public Cache(String cacheName, int cacheSize) throws Exception {
            super(cacheName, cacheSize);
        }

        @Override
        public boolean isCacheItemValid(String o) {
            return (null != o);
        }

        @Override
        public String loadData(String key) throws Exception {
            return ("data for key: " + key);
        }
        
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
