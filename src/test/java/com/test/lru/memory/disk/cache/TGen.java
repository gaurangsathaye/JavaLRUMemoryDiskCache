package com.test.lru.memory.disk.cache;

import com.lru.memory.disk.cache.DistributedConfigServer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author sathayeg
 */
public class TGen {

    static AtomicInteger count = new AtomicInteger(0);

    public static void main(String[] args) {
        try {
            //tRotate();
            tRotateDistConfigServerSevereErrors();
        } catch (Exception e) {
            p("error: " + e + ", cause: " + e.getCause());
        }
    }

    static void tRotate() {
        for (int i = 0; i < 10; i++) {
            p("rotate returned: " + rotate(count, 4));
        }

        p("count: " + count.get());
    }

    static int rotate(AtomicInteger ai, int rotateIn) {
        ai.set(((ai.get() % rotateIn) + 1));
        return ai.get();
    }

    static void tRotateDistConfigServerSevereErrors() throws Exception {
        DistributedConfigServer d = new DistributedConfigServer("host", "1");
        for (int i = 0; i < 14; i++) {
            d.setSevereErrorNextAttemptTimestamp();
            p(d.getErrNextAttemptTimestamp() - System.currentTimeMillis());
        }
    }

    static void p(Object o) {
        System.out.println(o);
    }
}
