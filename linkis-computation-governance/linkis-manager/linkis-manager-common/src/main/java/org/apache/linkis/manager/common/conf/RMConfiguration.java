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

package org.apache.linkis.manager.common.conf;

import org.apache.linkis.common.conf.ByteType;
import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.TimeType;

public class RMConfiguration {

  public static final CommonVars<Long> RM_WAIT_EVENT_TIME_OUT =
      CommonVars.apply("wds.linkis.rm.wait.event.time.out", 1000 * 60 * 12L);

  public static final CommonVars<TimeType> LOCK_RELEASE_TIMEOUT =
      CommonVars.apply("wds.linkis.manager.rm.lock.release.timeout", new TimeType("10m"));

  public static final CommonVars<TimeType> LOCK_RELEASE_CHECK_INTERVAL =
      CommonVars.apply("wds.linkis.manager.rm.lock.release.check.interval", new TimeType("5m"));

  public static final CommonVars<TimeType> LOCK_FAILED_LABEL_RESOURCE_DEAL_INTERVAL =
      CommonVars.apply("wds.linkis.manager.rm.lock.failed.deal.interval", new TimeType("10s"));

  // Resource parameter(资源参数)
  public static final CommonVars<Integer> USER_AVAILABLE_CPU =
      CommonVars.apply("wds.linkis.rm.client.core.max", 10);
  public static final CommonVars<ByteType> USER_AVAILABLE_MEMORY =
      CommonVars.apply("wds.linkis.rm.client.memory.max", new ByteType("20g"));
  public static final CommonVars<Integer> USER_AVAILABLE_INSTANCE =
      CommonVars.apply("wds.linkis.rm.instance", 10);

  public static final CommonVars<Integer> USER_AVAILABLE_YARN_INSTANCE_CPU =
      CommonVars.apply("wds.linkis.rm.yarnqueue.cores.max", 150);

  public static final CommonVars<ByteType> USER_AVAILABLE_YARN_INSTANCE_MEMORY =
      CommonVars.apply("wds.linkis.rm.yarnqueue.memory.max", new ByteType("450g"));

  public static final CommonVars<Integer> USER_AVAILABLE_YARN_INSTANCE =
      CommonVars.apply("wds.linkis.rm.yarnqueue.instance.max", 30);
  public static final CommonVars<String> USER_AVAILABLE_YARN_QUEUE_NAME =
      CommonVars.apply("wds.linkis.rm.yarnqueue", "default");
  public static final CommonVars<String> USER_AVAILABLE_CLUSTER_NAME =
      CommonVars.apply("wds.linkis.rm.cluster", "default");

  public static final CommonVars<Integer> USER_AVAILABLE_KUBERNETES_INSTANCE_CPU =
      CommonVars.apply("wds.linkis.rm.kubernetes.cores.max", 150000);
  public static final CommonVars<ByteType> USER_AVAILABLE_KUBERNETES_INSTANCE_MEMORY =
      CommonVars.apply("wds.linkis.rm.kubernetes.memory.max", new ByteType("450g"));
  public static final CommonVars<String> USER_AVAILABLE_KUBERNETES_INSTANCE_NAMESPACE =
      CommonVars.apply("wds.linkis.rm.kubernetes.namespace", "default");

  public static final CommonVars<Long> RM_ENGINE_SCAN_INTERVAL =
      CommonVars.apply("wds.linkis.rm.engine.scan.interval", 120000L);

  public static final CommonVars<String> DEFAULT_YARN_CLUSTER_NAME =
      CommonVars.apply("wds.linkis.rm.default.yarn.cluster.name", "default");
  public static final CommonVars<String> DEFAULT_YARN_TYPE =
      CommonVars.apply("wds.linkis.rm.default.yarn.cluster.type", "Yarn");
  public static final CommonVars<String> DEFAULT_KUBERNETES_CLUSTER_NAME =
      CommonVars.apply("wds.linkis.rm.default.kubernetes.cluster.name", "default");
  public static final CommonVars<String> DEFAULT_KUBERNETES_TYPE =
      CommonVars.apply("wds.linkis.rm.default.kubernetes.cluster.type", "K8S");
  public static final CommonVars<Integer> EXTERNAL_RETRY_NUM =
      CommonVars.apply("wds.linkis.rm.external.retry.num", 3);

  public static final CommonVars<Integer> LABEL_SERVICE_PARTITION_NUM =
      CommonVars.apply("wds.linkis.label.service.partition.num", 1000);

  public static final CommonVars<Integer> LABEL_SERVICE_INSTANCE_SHUFF_NUM =
      CommonVars.apply("wds.linkis.label.service.instance.shuff.num", 100);

  public static final CommonVars<Boolean> LABEL_SERVICE_INSTANCE_SHUFF_SWITCH =
      CommonVars.apply("wds.linkis.label.service.instance.shuff.switch", false);

  public static final CommonVars<Boolean> GET_RESOURCE_BY_LABEL_VALUE_ENABLED =
      CommonVars.apply("wds.linkis.get.resource.by.label.value.enable", false);
}
