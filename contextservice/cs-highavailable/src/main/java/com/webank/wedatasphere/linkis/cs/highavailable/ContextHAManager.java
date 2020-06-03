package com.webank.wedatasphere.linkis.cs.highavailable;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

/**
 * 动态代理实现持久层HAIDKey和contextID的动态转换
 *
 * @Author alexyang
 * @Date 2020/2/16
 */
public interface ContextHAManager {

    <T> T getContextHAProxy(T persistence) throws CSErrorException;

}
