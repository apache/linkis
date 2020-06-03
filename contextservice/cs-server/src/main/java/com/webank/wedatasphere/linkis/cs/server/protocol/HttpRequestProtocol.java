package com.webank.wedatasphere.linkis.cs.server.protocol;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface HttpRequestProtocol extends HttpProtocol {

    Object[] getRequestObjects();

    void setRequestObjects(Object[] objs);

    String getServiceName();

    void setUsername(String username);

    String getUsername();

    ServiceMethod getServiceMethod();

    void setServiceMethod(ServiceMethod method);

}
