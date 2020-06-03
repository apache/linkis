package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.protocol.RestResponseProtocol;

/**
 * Created by patinousward on 2020/2/22.
 */
public class RestJobBuilder extends HttpAnswerJobBuilder {

    @Override
    protected final HttpJob buildResponseProtocol(HttpJob job) {
        RestResponseProtocol protocol = new RestResponseProtocol();
        ((HttpAnswerJob) job).setResponseProtocol(protocol);
        return job;
    }

}
