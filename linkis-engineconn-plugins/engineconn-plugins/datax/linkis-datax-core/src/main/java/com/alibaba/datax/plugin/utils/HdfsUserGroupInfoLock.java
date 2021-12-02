package com.alibaba.datax.plugin.utils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * simple global LOCK
 * @author davidhua
 * 2019/4/24
 */
public class HdfsUserGroupInfoLock {
    private static ReentrantLock globallock = new ReentrantLock();

    public static void lock(){
        globallock.lock();
    }

    public static void unlock(){
        globallock.unlock();
    }
}
