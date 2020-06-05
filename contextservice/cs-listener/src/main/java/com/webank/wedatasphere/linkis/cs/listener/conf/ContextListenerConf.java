package com.webank.wedatasphere.linkis.cs.listener.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/25
 */
public class ContextListenerConf {
    public final static Integer WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_MAX = Integer.parseInt(CommonVars.apply("wds.linkis.cs.listener.asyn.consumer.thread.max","5").getValue());
    public final static Long WDS_CS_LISTENER_ASYN_CONSUMER_THREAD_FREE_TIME_MAX = Long.parseLong(CommonVars.apply("wds.linkis.cs.listener.asyn.consumer.freeTime.max","5000").getValue());
    public final static Integer WDS_CS_LISTENER_ASYN_QUEUE_CAPACITY =Integer.parseInt(CommonVars.apply("wds.linkis.cs.listener.asyn.queue.size.max","300").getValue()) ;
}
