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
import org.apache.linkis.manager.common.entity.metrics.NodeHealthyInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeOverLoadInfo;
import org.apache.linkis.manager.common.entity.metrics.NodeTaskInfo;
import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.label.entity.Label;

import java.util.Date;
import java.util.List;

public class AMEMNode implements EMNode, ScoreServiceInstance {

  private List<Label<?>> labels;

  private double score;

  private ServiceInstance serviceInstance;

  private NodeStatus nodeStatus;

  private NodeResource nodeResource;

  private String owner;

  private String mark;
  private String identifier;

  private String ticketId;

  private NodeTaskInfo nodeTaskInfo;

  private NodeOverLoadInfo nodeOverLoadInfo;

  private NodeHealthyInfo nodeHealthyInfo;

  private Date startTime;

  private Date updateTime;

  @Override
  public Date getUpdateTime() {
    return updateTime;
  }

  @Override
  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  @Override
  public Date getStartTime() {
    return startTime;
  }

  @Override
  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public AMEMNode() {}

  public AMEMNode(double score, ServiceInstance serviceInstance) {
    this.score = score;
    this.serviceInstance = serviceInstance;
  }

  @Override
  public List<Label<?>> getLabels() {
    return this.labels;
  }

  @Override
  public void setLabels(List<Label<?>> labels) {
    this.labels = labels;
  }

  @Override
  public double getScore() {
    return this.score;
  }

  @Override
  public void setScore(double score) {
    this.score = score;
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
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  @Override
  public String getMark() {
    return mark;
  }

  public void setMark(String mark) {
    this.mark = mark;
  }

  @Override
  public String getIdentifier() {
    return identifier;
  }

  @Override
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @Override
  public NodeResource getNodeResource() {
    return nodeResource;
  }

  @Override
  public void setNodeResource(NodeResource nodeResource) {
    this.nodeResource = nodeResource;
  }

  @Override
  public NodeTaskInfo getNodeTaskInfo() {
    return nodeTaskInfo;
  }

  @Override
  public void setNodeTaskInfo(NodeTaskInfo nodeTaskInfo) {
    this.nodeTaskInfo = nodeTaskInfo;
  }

  @Override
  public void setNodeOverLoadInfo(NodeOverLoadInfo nodeOverLoadInfo) {
    this.nodeOverLoadInfo = nodeOverLoadInfo;
  }

  @Override
  public NodeOverLoadInfo getNodeOverLoadInfo() {
    return nodeOverLoadInfo;
  }

  @Override
  public NodeHealthyInfo getNodeHealthyInfo() {
    return nodeHealthyInfo;
  }

  @Override
  public void setNodeHealthyInfo(NodeHealthyInfo nodeHealthyInfo) {
    this.nodeHealthyInfo = nodeHealthyInfo;
  }

  @Override
  public String getTicketId() {
    return ticketId;
  }

  @Override
  public void setTicketId(String ticketId) {
    this.ticketId = ticketId;
  }

  @Override
  public String toString() {
    return "AMEMNode{"
        + "labels="
        + labels
        + ", serviceInstance="
        + serviceInstance
        + ", nodeStatus="
        + nodeStatus
        + '}';
  }
}
