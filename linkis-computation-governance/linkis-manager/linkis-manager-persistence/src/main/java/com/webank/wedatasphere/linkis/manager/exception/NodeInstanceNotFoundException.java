package com.webank.wedatasphere.linkis.manager.exception;

/**
 * @Author: chaogefeng
 * @Date: 2020/7/13
 */
public class NodeInstanceNotFoundException extends PersistenceErrorException {
    public NodeInstanceNotFoundException(int errCode, String desc) {
        super(errCode, desc);
    }
}
