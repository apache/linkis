package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.protocol.HttpRequestProtocol;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface HttpJob {

    HttpRequestProtocol getRequestProtocol();

    void setRequestProtocol(HttpRequestProtocol protocol);

}
