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

package org.apache.linkis.manager.common.entity.enumeration;

public enum NodeHealthy {

  /**
   * 节点监控状态信息 to monitor node status info. Healthy：状态正常 UnHealthy： EM自己标识自己为UnHealthy 或者
   * manager把他标识为UnHealthy 处理引擎状态不正常，manager主动要求所有的engine强制退出（engine自杀） WARN： 引擎处于告警状态，但是可以接受任务
   * StockAvailable： 存量可用状态，可以接受任务。当EM状态最近n次心跳没有上报，但是已经启动的Engine还是正常的可以接受任务 StockUnavailable：
   * 存量不可用状态，不可以接受任务。（超过n+1没上报心跳）或者（EM自己判断，但是服务正常的情况），但是如果往上面提交任务会出现error失败情况 EM
   * 处于StockUnavailable时，manager主动要求所有的engine非强制退出，manager需要将
   * EM标识为UnHealthy。如果StockUnavailable状态如果超过n分钟，则发送IMS告警
   */
  Healthy,
  UnHealthy,
  WARN,
  StockAvailable,
  StockUnavailable;

  public static Boolean isAvailable(NodeHealthy nodeHealthy) {
    if (Healthy == nodeHealthy || WARN == nodeHealthy) {
      return true;
    }
    return false;
  }
}
