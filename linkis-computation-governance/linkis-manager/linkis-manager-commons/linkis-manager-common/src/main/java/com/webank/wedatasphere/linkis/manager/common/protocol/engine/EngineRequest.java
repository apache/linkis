package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;


public interface EngineRequest extends RequestProtocol {
    String getUser();
}
