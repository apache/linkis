package com.webank.wedatasphere.linkis.cs.listener.callback;

/**
 * @Author: chaogefeng
 * @Date: 2020/2/20
 */
public interface AbstractCallbackEngine extends CallbackEngine {
    //todo
    //实现事件的存储和按需消费:存储这些变化的事件，并且按需消费
    //事件超过一定时间还没被消费，自动移除
    //cskey被五个client注册了listener，如果有挂掉，那么必须要一个最大消费时间的机制
}
