package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;

/**
 * Created by patinousward on 2020/2/22.
 */
public abstract class HttpJobBuilder {

    public HttpJob build(ServiceType serviceType) {
        return buildRequestProtocol(createHttpJob(), serviceType);
    }

    private HttpJob buildRequestProtocol(HttpJob job, ServiceType serviceType) {
        job.setRequestProtocol(serviceType.getRequestProtocol());
        return job;
    }

    protected abstract HttpJob createHttpJob();

}
