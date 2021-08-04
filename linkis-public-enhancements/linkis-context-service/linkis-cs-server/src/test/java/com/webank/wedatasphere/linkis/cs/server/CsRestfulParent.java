/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.cs.server;

import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceMethod;
import com.webank.wedatasphere.linkis.cs.server.enumeration.ServiceType;
import com.webank.wedatasphere.linkis.cs.server.protocol.HttpRequestProtocol;
import com.webank.wedatasphere.linkis.cs.server.protocol.HttpResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.protocol.RestResponseProtocol;
import com.webank.wedatasphere.linkis.cs.server.scheduler.CsScheduler;
import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpAnswerJob;
import com.webank.wedatasphere.linkis.cs.server.scheduler.RestJobBuilder;
import com.webank.wedatasphere.linkis.server.Message;
import com.webank.wedatasphere.linkis.server.security.SecurityFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

public interface CsRestfulParent {

    default HttpAnswerJob submitRestJob(HttpServletRequest req,
                                        ServiceMethod method,
                                        Object... objects) throws InterruptedException {
        HttpAnswerJob job = (HttpAnswerJob) new RestJobBuilder().build(getServiceType());
        HttpRequestProtocol protocol = job.getRequestProtocol();
        protocol.setUsername(SecurityFilter.getLoginUsername(req));
        protocol.setServiceMethod(method);
        protocol.setRequestObjects(objects);
        getScheduler().submit(job);
        return job;
    }

    default Response generateResponse(HttpAnswerJob job, String responseKey) {
        HttpResponseProtocol responseProtocol = job.getResponseProtocol();
        if (responseProtocol instanceof RestResponseProtocol) {
            Message message = ((RestResponseProtocol) responseProtocol).get();
            if (message == null) {
                return Message.messageToResponse(Message.error("job execute timeout"));
            }
            int status = ((RestResponseProtocol) responseProtocol).get().getStatus();
            if (status == 1) {
                //failed
                return Message.messageToResponse(((RestResponseProtocol) responseProtocol).get());
            } else if (status == 0) {
                Object data = job.getResponseProtocol().getResponseData();
                return Message.messageToResponse(Message.ok().data(responseKey, data));
            } else {

            }
        }
        return Message.messageToResponse(Message.ok());
    }

    ServiceType getServiceType();

    CsScheduler getScheduler();

}
