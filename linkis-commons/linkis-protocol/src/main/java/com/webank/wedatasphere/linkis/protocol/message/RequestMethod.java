package com.webank.wedatasphere.linkis.protocol.message;


public interface RequestMethod {

    default String method() {
        return null;
    }

}
