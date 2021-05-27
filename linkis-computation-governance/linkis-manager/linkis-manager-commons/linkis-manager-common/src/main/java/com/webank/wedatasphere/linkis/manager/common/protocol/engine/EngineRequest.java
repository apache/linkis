package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

/**
 * @author peacewong
 * @date 2020/6/10 17:17
 */
public interface EngineRequest extends RequestProtocol {
    String getUser();
}
