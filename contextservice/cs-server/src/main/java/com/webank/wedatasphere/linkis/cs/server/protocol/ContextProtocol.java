package com.webank.wedatasphere.linkis.cs.server.protocol;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;

/**
 * Created by patinousward on 2020/2/22.
 */
public class ContextProtocol extends AbstractHttpRequestProtocol {

    @Override
    public String getServiceName() {
        return ServiceType.CONTEXT.name();
    }
}
