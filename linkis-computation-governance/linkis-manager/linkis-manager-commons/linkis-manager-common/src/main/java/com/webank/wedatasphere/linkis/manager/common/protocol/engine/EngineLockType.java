package com.webank.wedatasphere.linkis.manager.common.protocol.engine;


public enum EngineLockType {

    /**
     * Always - 除非主动释放，否则不释放 release unless one release positively
     * Once - 加锁后访问一次便释放，且超时也会释放 released when visited at once
     * Timed - 加锁后，超时或主动释放都会释放  released when timeout
     */
    Always,
    Once,
    Timed

}
