/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
