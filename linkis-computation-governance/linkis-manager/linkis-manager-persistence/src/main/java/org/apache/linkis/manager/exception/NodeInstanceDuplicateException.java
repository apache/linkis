package org.apache.linkis.manager.exception;


public class NodeInstanceDuplicateException  extends PersistenceErrorException {
    public NodeInstanceDuplicateException(int errCode, String desc) {
        super(errCode, desc);
    }
}
