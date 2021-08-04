package com.webank.wedatasphere.linkis.manager.exception;


public class NodeInstanceDuplicateException  extends PersistenceErrorException {
    public NodeInstanceDuplicateException(int errCode, String desc) {
        super(errCode, desc);
    }
}
