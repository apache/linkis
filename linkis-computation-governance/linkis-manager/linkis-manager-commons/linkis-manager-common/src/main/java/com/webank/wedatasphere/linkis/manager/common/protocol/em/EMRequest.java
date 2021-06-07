package com.webank.wedatasphere.linkis.manager.common.protocol.em;

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;


public interface EMRequest extends RequestProtocol {
    String getUser();
}
