java -cp target/JavaLRUMemoryDiskCache-1.2.jar -Dport=23290 -Dcache.size=50000 -Ddisk.cache.dir="./standalone/cache" -Dserver.threads=200 com.lru.memory.disk.cache.Server

