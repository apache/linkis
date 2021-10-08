package com.apache.wedatasphere.linkis.manager.exception;


public class NodeInstanceNotFoundException extends PersistenceErrorException {
    public NodeInstanceNotFoundException(int errCode, String desc) {
        super(errCode, desc);
    }
}
