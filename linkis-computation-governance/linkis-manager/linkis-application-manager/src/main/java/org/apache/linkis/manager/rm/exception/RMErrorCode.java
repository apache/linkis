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

package org.apache.linkis.manager.rm.exception;

import org.apache.linkis.common.errorcode.LinkisErrorCode;

public enum RMErrorCode implements LinkisErrorCode {

  /** */
  LABEL_RESOURCE_NOT_FOUND(110021, "label resource not found, please check!(未找到标签资源，请检查！)"),

  LOCK_LABEL_FAILED(
      110022,
      "lock label failed! {0} over {1} ms,please wait a moment and try again!(锁定标签失败！ {0} 超过 {1} 毫秒，请稍等片刻，然后重试！)"),

  LABEL_DUPLICATED(
      110019,
      "Label has exist! {0} has been registered in {1}, cannot be updated to {2}(标签已存在！ {0} 已在 {1} 注册，无法更新到 {2})"),

  DRIVER_MEMORY_INSUFFICIENT(
      12004,
      "Drive memory resources are insufficient, to reduce the drive memory(内存资源不足，建议调小驱动内存)"),

  DRIVER_CPU_INSUFFICIENT(
      12005,
      "Drive core resources are insufficient, to reduce the number of driver cores(CPU资源不足，建议调小驱动核数)"),

  INSTANCES_INSUFFICIENT(
      12006, "Insufficient number of instances, idle engines can be killed(实例数不足，可以kill空闲的引擎)"),

  QUEUE_MEMORY_INSUFFICIENT(
      12002,
      "Insufficient queue memory resources, reduce the executor memory(queue内存资源不足，减少executor内存)"),

  QUEUE_CPU_INSUFFICIENT(
      12001,
      "Queue CPU resources are insufficient, reduce the number of executors.(队列CPU资源不足，建议调小执行器个数)"),

  QUEUE_INSTANCES_INSUFFICIENT(
      12003,
      "Insufficient number of queue instances, idle engines can be killed(队列实例数不足，可以kill空闲的引擎)"),

  CLUSTER_QUEUE_MEMORY_INSUFFICIENT(12010, "Insufficient cluster queue memory(集群队列内存不足)"),

  CLUSTER_QUEUE_CPU_INSUFFICIENT(12011, "Insufficient cluster queue cpu(集群队列cpu不足)"),

  CLUSTER_QUEUE_INSTANCES_INSUFFICIENT(12012, "Insufficient cluster queue instance(集群队列实例不足)"),

  ECM_RESOURCE_INSUFFICIENT(11000, "ECM resources are insufficient(ECM 资源不足)"),

  ECM_MEMORY_INSUFFICIENT(11001, "ECM memory resources are insufficient(ECM 内存资源不足)"),

  ECM_CPU_INSUFFICIENT(11002, "ECM CPU resources are insufficient(ECM CPU资源不足)"),

  ECM_INSTANCES_INSUFFICIENT(11003, "ECM Insufficient number of instances(ECM实例数不足)"),
  ;

  private final int errorCode;

  private final String errorDesc;

  RMErrorCode(int errorCode, String errorDesc) {
    this.errorCode = errorCode;
    this.errorDesc = errorDesc;
  }

  @Override
  public int getErrorCode() {
    return errorCode;
  }

  @Override
  public String getErrorDesc() {
    return errorDesc;
  }
}
