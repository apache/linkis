/*
 *
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
 *
 */

package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.protocol.message.RequestMethod;


public class EngineStopRequest implements EngineRequest, RequestMethod {

    private ServiceInstance serviceInstance;

    private String user;

    public EngineStopRequest() {

    }

    public EngineStopRequest(ServiceInstance serviceInstance, String user) {
        this.serviceInstance = serviceInstance;
        this.user = user;
    }

    public ServiceInstance getServiceInstance() {
        return serviceInstance;
    }

    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public String method() {
        return "/engine/stop";
    }

    @Override
    public String toString() {
        return "EngineStopRequest{" +
                "serviceInstance=" + serviceInstance +
                ", user='" + user + '\'' +
                '}';
    }
}
