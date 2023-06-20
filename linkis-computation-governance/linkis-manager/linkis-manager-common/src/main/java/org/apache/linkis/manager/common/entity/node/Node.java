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

package org.apache.linkis.manager.common.entity.node;

import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.manager.common.entity.enumeration.NodeStatus;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.util.Date;

public interface Node extends RequestProtocol {

  ServiceInstance getServiceInstance();

  void setServiceInstance(ServiceInstance serviceInstance);

  NodeStatus getNodeStatus();

  void setNodeStatus(NodeStatus status);

  String getOwner();

  String getMark();

  Date getUpdateTime();

  void setUpdateTime(Date updateTime);

  Date getStartTime();

  void setStartTime(Date startTime);

  String getIdentifier();

  void setIdentifier(String identifier);

  String getTicketId();

  void setTicketId(String ticketId);
}
