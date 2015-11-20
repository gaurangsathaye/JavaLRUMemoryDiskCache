java -Dport=23290 -Dcache.size=50000 -Ddisk.cache.dir="./standalone/cache1" -Dserver.threads=70 -Dlog.file=server1.log -jar target/JavaLRUMemoryDiskCache-1.2.jar  &

java -Dport=23291 -Dcache.size=50000 -Ddisk.cache.dir="./standalone/cache2" -Dserver.threads=70  -Dlog.file=server2.log -jar target/JavaLRUMemoryDiskCache-1.2.jar &



