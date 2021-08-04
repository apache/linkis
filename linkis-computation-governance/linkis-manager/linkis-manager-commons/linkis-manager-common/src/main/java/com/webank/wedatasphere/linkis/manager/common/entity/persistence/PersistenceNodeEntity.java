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

package com.webank.wedatasphere.linkis.manager.common.entity.persistence;

import com.webank.wedatasphere.linkis.common.ServiceInstance;
import com.webank.wedatasphere.linkis.manager.common.entity.enumeration.NodeStatus;
import com.webank.wedatasphere.linkis.manager.common.entity.node.Node;

import java.util.Date;


public class PersistenceNodeEntity implements Node {

    private ServiceInstance serviceInstance;
    private String owner;
    private String mark;
    private NodeStatus nodeStatus;

    private Date startTime;

    public Date getStartTime() {
        return startTime;
    }


    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }


    @Override
    public ServiceInstance getServiceInstance() {
        return this.serviceInstance;
    }

    @Override
    public void setServiceInstance(ServiceInstance serviceInstance) {
        this.serviceInstance = serviceInstance;
    }

    @Override
    public NodeStatus getNodeStatus() {
        return this.nodeStatus;
    }

    @Override
    public void setNodeStatus(NodeStatus status) {
        this.nodeStatus = status;
    }

    @Override
    public String getOwner() {
        return this.owner;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String getMark() {
        return this.mark;
    }

    public void setOwner(String owner){
        this.owner = owner;
    }
}
