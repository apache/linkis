package com.alibaba.datax.common.plugin;

/**
 * @author davidhua
 * 2019/8/20
 */
public  interface BasicDataReceiver<T> {
    /**
     * get data from reader(channel actually)
     * @return
     */
    T getFromReader();

    /**
     * shutdown the channel
     */
    void shutdown();
}
