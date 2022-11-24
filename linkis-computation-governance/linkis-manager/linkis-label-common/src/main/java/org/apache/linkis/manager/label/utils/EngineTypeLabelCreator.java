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

package org.apache.linkis.manager.label.utils;

import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;
import org.apache.linkis.manager.label.entity.engine.EngineType;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class EngineTypeLabelCreator {

  private static LabelBuilderFactory labelBuilderFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  private static Map<String, String> defaultVersion = null;

  static {
    init();
  }

  private static void init() {
    if (null == defaultVersion) {
      synchronized (EngineTypeLabelCreator.class) {
        if (null == defaultVersion) {
          defaultVersion = new HashMap<>(16);
          defaultVersion.put(
              EngineType.SPARK().toString(), LabelCommonConfig.SPARK_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.HIVE().toString(), LabelCommonConfig.HIVE_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.PYTHON().toString(), LabelCommonConfig.PYTHON_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.IO_ENGINE_FILE().toString(),
              LabelCommonConfig.FILE_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.IO_ENGINE_HDFS().toString(),
              LabelCommonConfig.HDFS_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.JDBC().toString(), LabelCommonConfig.JDBC_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.PIPELINE().toString(),
              LabelCommonConfig.PIPELINE_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.SHELL().toString(), LabelCommonConfig.SHELL_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.APPCONN().toString(), LabelCommonConfig.APPCONN_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.FLINK().toString(), LabelCommonConfig.FLINK_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.PRESTO().toString(), LabelCommonConfig.PRESTO_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.SQOOP().toString(), LabelCommonConfig.SQOOP_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.DATAX().toString(), LabelCommonConfig.DATAX_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.OPENLOOKENG().toString(),
              LabelCommonConfig.OPENLOOKENG_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.TRINO().toString(),
              LabelCommonConfig.TRINO_ENGINE_CONN_VERSION.getValue());
          defaultVersion.put(
              EngineType.ELASTICSEARCH().toString(),
              LabelCommonConfig.ELASTICSEARCH_ENGINE_VERSION.getValue());
          defaultVersion.put(
              EngineType.SEATUNNEL().toString(),
              LabelCommonConfig.SEATUNNEL_ENGINE_CONN_VERSION.getValue());
          defaultVersion.put("*", "*");
        }
      }
    }
  }

  public static final String ENGINE_TYPE = "engineType";

  public static EngineTypeLabel createEngineTypeLabel(String type) {
    if (null == defaultVersion) {
      init();
    }
    EngineTypeLabel label = labelBuilderFactory.createLabel(EngineTypeLabel.class);
    label.setEngineType(type);
    String version = defaultVersion.get(type);
    if (StringUtils.isNotBlank(version)) {
      label.setVersion(version);
    } else {
      label.setVersion("*");
    }
    return label;
  }

  public static void registerVersion(String type, String version) {
    if (null == defaultVersion) {
      init();
    }
    defaultVersion.put(type, version);
  }
}
