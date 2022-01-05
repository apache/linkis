/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.message.builder;

import org.apache.linkis.message.context.AbstractMessageSchedulerContext;
import org.apache.linkis.message.scheduler.MethodExecuteWrapper;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.List;
import java.util.Map;



public interface MessageJob extends Runnable, Future<Object> {

    RequestProtocol getRequestProtocol();

    void setRequestProtocol(RequestProtocol requestProtocol);

    Map<String, List<MethodExecuteWrapper>> getMethodExecuteWrappers();

    void setMethodExecuteWrappers(Map<String, List<MethodExecuteWrapper>> methodExecuteWrappers);

    ServiceMethodContext getMethodContext();

    void setMethodContext(ServiceMethodContext smc);

    AbstractMessageSchedulerContext getContext();

    void setContext(AbstractMessageSchedulerContext context);

}
