/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cs.server;

import org.apache.linkis.cs.server.enumeration.ServiceMethod;
import org.apache.linkis.cs.server.enumeration.ServiceType;
import org.apache.linkis.cs.server.protocol.HttpRequestProtocol;
import org.apache.linkis.cs.server.protocol.HttpResponseProtocol;
import org.apache.linkis.cs.server.protocol.RestResponseProtocol;
import org.apache.linkis.cs.server.scheduler.CsScheduler;
import org.apache.linkis.cs.server.scheduler.HttpAnswerJob;
import org.apache.linkis.cs.server.scheduler.RestJobBuilder;
import org.apache.linkis.server.Message;
import org.apache.linkis.server.utils.ModuleUserUtils;

import javax.servlet.http.HttpServletRequest;

public interface CsRestfulParent {

  default HttpAnswerJob submitRestJob(
      HttpServletRequest req, ServiceMethod method, Object... objects) throws InterruptedException {
    HttpAnswerJob job = (HttpAnswerJob) new RestJobBuilder().build(getServiceType());
    HttpRequestProtocol protocol = job.getRequestProtocol();
    protocol.setUsername(ModuleUserUtils.getOperationUser(req));
    protocol.setServiceMethod(method);
    protocol.setRequestObjects(objects);
    getScheduler().submit(job);
    return job;
  }

  default Message generateResponse(HttpAnswerJob job, String responseKey) {
    HttpResponseProtocol responseProtocol = job.getResponseProtocol();
    if (responseProtocol instanceof RestResponseProtocol) {
      Message message = ((RestResponseProtocol) responseProtocol).get();
      if (message == null) {
        return Message.error("job execute timeout");
      }
      int status = ((RestResponseProtocol) responseProtocol).get().getStatus();
      if (status == 1) {
        // failed
        return ((RestResponseProtocol) responseProtocol).get();
      } else if (status == 0) {
        Object data = job.getResponseProtocol().getResponseData();
        return Message.ok().data(responseKey, data);
      } else {

      }
    }
    return Message.ok();
  }

  ServiceType getServiceType();

  CsScheduler getScheduler();
}
