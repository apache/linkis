package com.webank.wedatasphere.linkis.cs.server.scheduler;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.protocol.HttpRequestProtocol;
import com.webank.wedatasphere.linkis.cs.server.protocol.HttpResponseProtocol;

/**
 * Created by patinousward on 2020/2/22.
 */
public abstract class HttpAnswerJobBuilder extends HttpJobBuilder {

    @Override
    public final HttpJob build(ServiceType serviceType) {
        return buildResponseProtocol(super.build(serviceType));
    }

    @Override
    protected final HttpJob createHttpJob() {
        return new DefaultHttpAnswerJob();
    }

    protected abstract HttpJob buildResponseProtocol(HttpJob job);

    private static class DefaultHttpAnswerJob implements HttpAnswerJob {
        private HttpRequestProtocol httpRequestProtocol;

        private HttpResponseProtocol httpResponseProtocol;

        @Override
        public HttpRequestProtocol getRequestProtocol() {
            return this.httpRequestProtocol;
        }

        @Override
        public void setRequestProtocol(HttpRequestProtocol protocol) {
            this.httpRequestProtocol = protocol;
        }

        @Override
        public HttpResponseProtocol getResponseProtocol() {
            return this.httpResponseProtocol;
        }

        @Override
        public void setResponseProtocol(HttpResponseProtocol protocol) {
            this.httpResponseProtocol = protocol;
        }
    }

}
