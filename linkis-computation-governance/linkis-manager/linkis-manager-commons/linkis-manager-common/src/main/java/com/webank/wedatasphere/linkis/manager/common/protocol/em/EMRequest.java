package com.webank.wedatasphere.linkis.manager.common.protocol.em;

import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

/**
 * @author peacewong
 * @date 2020/6/10 17:24
 */
public interface EMRequest extends RequestProtocol {
    String getUser();
}
