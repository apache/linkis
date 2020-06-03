package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.protocol.HttpResponseProtocol;

/**
 * Created by patinousward on 2020/2/18.
 */
public interface HttpAnswerJob extends HttpJob {

    HttpResponseProtocol getResponseProtocol();

    void setResponseProtocol(HttpResponseProtocol protocol);

}
