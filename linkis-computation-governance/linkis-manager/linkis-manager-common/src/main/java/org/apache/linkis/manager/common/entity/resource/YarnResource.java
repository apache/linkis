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

package org.apache.linkis.manager.common.entity.resource;

import org.apache.linkis.common.utils.ByteTimeUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Queue resource information, no initial registration, only user resource usage limit
 * 队列资源信息，无初始化注册，只有用户资源使用上限
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class YarnResource extends Resource {
  private final long queueMemory;
  private final int queueCores;
  private final int queueInstances;
  private final String queueName;
  private final String applicationId;

  public YarnResource(
      long queueMemory,
      int queueCores,
      int queueInstances,
      String queueName,
      String applicationId) {
    this.queueMemory = queueMemory;
    this.queueCores = queueCores;
    this.queueInstances = queueInstances;
    this.queueName = queueName;
    this.applicationId = applicationId;
  }

  public YarnResource() {
    this(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "default", "");
  }

  public YarnResource(long queueMemory, int queueCores, int queueInstances, String queueName) {
    this(queueMemory, queueCores, queueInstances, queueName, "");
  }

  YarnResource toYarnResource(Resource r) {
    if (r instanceof YarnResource) {
      return (YarnResource) r;
    } else {
      return new YarnResource(Long.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, "default");
    }
  }

  public long getQueueMemory() {
    return queueMemory;
  }

  public int getQueueCores() {
    return queueCores;
  }

  public int getQueueInstances() {
    return queueInstances;
  }

  public String getQueueName() {
    return queueName;
  }

  public String getApplicationId() {
    return applicationId;
  }

  public YarnResource add(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return new YarnResource(
        queueMemory + r.getQueueMemory(),
        queueCores + r.getQueueCores(),
        queueInstances + r.getQueueInstances(),
        r.getQueueName());
  }

  public YarnResource minus(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return new YarnResource(
        queueMemory - r.getQueueMemory(),
        queueCores - r.getQueueCores(),
        queueInstances - r.getQueueInstances(),
        r.getQueueName());
  }

  public YarnResource multiplied(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return new YarnResource(
        queueMemory * r.getQueueMemory(),
        queueCores * r.getQueueCores(),
        queueInstances * r.getQueueInstances(),
        r.getQueueName());
  }

  public YarnResource multiplied(float rate) {
    return new YarnResource(
        (long) (queueMemory * rate),
        (int) (queueCores * rate),
        (int) (queueInstances * rate),
        queueName);
  }

  public YarnResource divide(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return new YarnResource(
        queueMemory / r.getQueueMemory(),
        queueCores / r.getQueueCores(),
        queueInstances / r.getQueueInstances(),
        r.getQueueName());
  }

  public YarnResource divide(int rate) {
    return new YarnResource(
        queueMemory / rate, queueCores / rate, queueInstances / rate, queueName);
  }

  public boolean moreThan(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return queueMemory > r.getQueueMemory() && queueCores > r.getQueueCores();
  }

  public boolean caseMore(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return queueMemory > r.getQueueMemory() || queueCores > r.getQueueCores();
  }

  public boolean equalsTo(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return queueName.equals(r.getQueueName())
        && queueMemory == r.getQueueMemory()
        && queueCores == r.getQueueCores()
        && queueInstances == r.getQueueInstances();
  }

  @Override
  public boolean notLess(Resource resource) {
    YarnResource r = toYarnResource(resource);
    return queueMemory >= r.getQueueMemory() && queueCores >= r.getQueueCores();
  }

  @Override
  public boolean less(Resource r) {
    return !notLess(r);
  }

  @Override
  public int compare(Resource resource) {
    YarnResource r = toYarnResource(resource);
    if (this.getQueueMemory() > r.getQueueMemory()) {
      return 1;
    } else if (this.getQueueMemory() < r.getQueueMemory()) {
      return -1;
    } else {
      return Integer.compare(this.getQueueCores(), r.getQueueCores());
    }
  }

  @Override
  public String toJson() {
    return String.format(
        "{\"queueName\":\"%s\",\"queueMemory\":\"%s\",\"queueCpu\":%d, \"instance\":%d}",
        queueName, ByteTimeUtils.bytesToString(queueMemory), queueCores, queueInstances);
  }

  @Override
  public String toString() {
    return String.format(
        "Queue name(队列名)：%s，Queue memory(队列内存)：%s，Queue core number(队列核数)：%d, Number of queue instances(队列实例数)：%d",
        queueName, ByteTimeUtils.bytesToString(queueMemory), queueCores, queueInstances);
  }
}
