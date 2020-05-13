package com.fishblack.async.job;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread Factory in DSS Job Management Framework.
 */
public class JobThreadFactory implements ThreadFactory {

    private String threadNamePrefix;
    private AtomicInteger count = new AtomicInteger(0);

    public JobThreadFactory(String threadNamePrefix){
        this.threadNamePrefix = threadNamePrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        String threadName = threadNamePrefix + "[" + count.addAndGet(1) + "]";
        t.setName(threadName);
        return t;
    }
}
