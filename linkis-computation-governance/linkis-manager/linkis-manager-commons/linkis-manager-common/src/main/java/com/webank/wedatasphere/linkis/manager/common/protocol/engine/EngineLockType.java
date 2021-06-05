package com.webank.wedatasphere.linkis.manager.common.protocol.engine;


public enum EngineLockType {

    /**
     * Always - 除非主动释放，否则不释放
     * Once - 加锁后访问一次便释放，且超时也会释放
     * Timed - 加锁后，超时或主动释放都会释放
     */
    Always,
    Once,
    Timed

}
