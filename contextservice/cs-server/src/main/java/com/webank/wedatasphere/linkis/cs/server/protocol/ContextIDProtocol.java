package com.webank.wedatasphere.linkis.cs.server.protocol;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;

/**
 * Created by patinousward on 2020/2/18.
 */
public class ContextIDProtocol extends AbstractHttpRequestProtocol {
    @Override
    public String getServiceName() {
        return ServiceType.CONTEXT_ID.name();
    }
}
