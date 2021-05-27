package com.webank.wedatasphere.linkis.manager.exception;

/**
 * @Author: chaogefeng
 * @Date: 2020/7/13
 */
public class NodeInstanceDuplicateException  extends PersistenceErrorException {
    public NodeInstanceDuplicateException(int errCode, String desc) {
        super(errCode, desc);
    }
}
