package com.webank.wedatasphere.linkis.cs.server.protocol;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;

/**
 * Created by patinousward on 2020/2/18.
 */
public abstract class AbstractHttpRequestProtocol implements HttpRequestProtocol {

    private Object[] requestObjects;

    private String username;

    private ServiceMethod serviceMethod;

    @Override
    public Object[] getRequestObjects() {
        return requestObjects;
    }

    @Override
    public void setRequestObjects(Object[] requestObjects) {
        this.requestObjects = requestObjects;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public ServiceMethod getServiceMethod() {
        return serviceMethod;
    }

    @Override
    public void setServiceMethod(ServiceMethod serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }
}
