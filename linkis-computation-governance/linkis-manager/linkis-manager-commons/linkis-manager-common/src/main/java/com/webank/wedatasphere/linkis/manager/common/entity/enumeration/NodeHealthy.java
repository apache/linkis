package com.webank.wedatasphere.linkis.manager.common.entity.enumeration;

/**
 * @author peacewong
 * @date 2020/7/9 16:07
 */
public enum NodeHealthy {

    /**
     * 节点监控状态信息
     * Healthy：状态正常
     * UnHealthy： EM自己标识自己为UnHealthy 或者 manager把他标识为UnHealthy 处理引擎状态不正常，manager主动要求所有的engine强制退出（engine自杀）
     * WARN： 引擎处于告警状态，但是可以接受任务
     * StockAvailable： 存量可用状态，可以接受任务。当EM状态最近n次心跳没有上报，但是已经启动的Engine还是正常的可以接受任务
     * StockUnavailable： 存量不可用状态，不可以接受任务。（超过n+1没上报心跳）或者（EM自己判断，但是服务正常的情况），但是如果往上面提交任务会出现error失败情况
     * EM 处于StockUnavailable时，manager主动要求所有的engine非强制退出，manager需要将 EM标识为UnHealthy。如果StockUnavailable状态如果超过n分钟，则发送IMS告警
     */
    Healthy, UnHealthy, WARN, StockAvailable, StockUnavailable;

    public static Boolean isAvailable(NodeHealthy nodeHealthy) {
        if (Healthy == nodeHealthy || WARN == nodeHealthy || StockAvailable == nodeHealthy) {
            return true;
        }
        return false;
    }
}
