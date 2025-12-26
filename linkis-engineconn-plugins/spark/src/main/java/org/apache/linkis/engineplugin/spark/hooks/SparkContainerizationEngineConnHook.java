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

package org.apache.linkis.engineplugin.spark.hooks;

import org.apache.linkis.engineconn.common.creation.EngineCreationContext;
import org.apache.linkis.engineconn.common.engineconn.EngineConn;
import org.apache.linkis.engineconn.common.hook.EngineConnHook;
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkContainerizationEngineConnHook implements EngineConnHook {
  private static final Logger logger =
      LoggerFactory.getLogger(SparkContainerizationEngineConnHook.class);

  @Override
  public void beforeCreateEngineConn(EngineCreationContext engineCreationContext) {
    Map<String, String> options = engineCreationContext.getOptions();
    String mappingHost =
        SparkConfiguration.ENGINE_CONN_CONTAINERIZATION_MAPPING_HOST().getValue(options);
    String mappingPorts =
        SparkConfiguration.ENGINE_CONN_CONTAINERIZATION_MAPPING_PORTS().getValue(options);
    List<String> mappingPortList =
        Arrays.stream(mappingPorts.trim().split(","))
            .filter(StringUtils::isNoneEmpty)
            .collect(Collectors.toList());

    if (mappingPortList.size() == 2) {
      logger.info(
          "加载spark容器化配置, spark.driver.host={}, spark.driver.port={}, spark.driver.blockManager.port={}",
          mappingHost,
          mappingPortList.get(0),
          mappingPortList.get(1));
      options.put(SparkConfiguration.SPARK_DRIVER_HOST().key(), mappingHost);
      options.put(
          SparkConfiguration.SPARK_DRIVER_BIND_ADDRESS().key(),
          SparkConfiguration.SPARK_DRIVER_BIND_ADDRESS().defaultValue());
      options.put(SparkConfiguration.SPARK_DRIVER_PORT().key(), mappingPortList.get(0));
      options.put(
          SparkConfiguration.SPARK_DRIVER_BLOCK_MANAGER_PORT().key(), mappingPortList.get(1));
    }
  }

  @Override
  public void beforeExecutionExecute(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {}

  @Override
  public void afterExecutionExecute(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {}

  @Override
  public void afterEngineServerStartFailed(
      EngineCreationContext engineCreationContext, Throwable throwable) {}

  @Override
  public void afterEngineServerStartSuccess(
      EngineCreationContext engineCreationContext, EngineConn engineConn) {}
}
