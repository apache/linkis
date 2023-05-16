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

package org.apache.linkis.engineconnplugin.flink.operator;

import org.apache.linkis.common.exception.WarnException;
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager$;
import org.apache.linkis.engineconnplugin.flink.client.deployment.ClusterDescriptorAdapter;
import org.apache.linkis.engineconnplugin.flink.errorcode.FlinkErrorCodeSummary;
import org.apache.linkis.engineconnplugin.flink.exception.JobExecutionException;
import org.apache.linkis.engineconnplugin.flink.executor.FlinkOnceExecutor;
import org.apache.linkis.manager.common.operator.Operator;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerSavepointOperator implements Operator {

  private static final Logger logger = LoggerFactory.getLogger(TriggerSavepointOperator.class);

  @Override
  public String[] getNames() {
    return new String[] {"doSavepoint"};
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> parameters) {
    String savepoint = getAsThrow(parameters, "savepointPath");
    String mode = getAsThrow(parameters, "mode");
    logger.info("try to " + mode + " savepoint with path " + savepoint + ".");

    if (OnceExecutorManager$.MODULE$.getInstance().getReportExecutor()
        instanceof FlinkOnceExecutor) {
      FlinkOnceExecutor flinkExecutor =
          (FlinkOnceExecutor) OnceExecutorManager$.MODULE$.getInstance().getReportExecutor();
      ClusterDescriptorAdapter clusterDescriptorAdapter =
          (ClusterDescriptorAdapter) flinkExecutor.getClusterDescriptorAdapter();
      String writtenSavepoint = "";
      try {
        writtenSavepoint = clusterDescriptorAdapter.doSavepoint(savepoint, mode);
      } catch (JobExecutionException e) {
        logger.info("doSavepoint failed", e);
        throw new RuntimeException(e);
      }

      Map<String, Object> stringMap = new HashMap<>();
      stringMap.put("writtenSavepoint", writtenSavepoint);
      return stringMap;
    } else {
      throw new WarnException(
          FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorCode(),
          MessageFormat.format(
              FlinkErrorCodeSummary.NOT_SUPPORT_SAVEPOTION.getErrorDesc(),
              OnceExecutorManager$.MODULE$
                  .getInstance()
                  .getReportExecutor()
                  .getClass()
                  .getSimpleName()));
    }
  }

  public <T> T getAsThrow(Map<String, Object> parameters, String key) {
    Object value = parameters.get(key);
    if (value != null) {
      try {
        return (T) value;
      } catch (Exception e) {
        throw new IllegalArgumentException("parameter " + key + " is invalid.", e);
      }
    } else {
      throw new IllegalArgumentException("parameter " + key + " is required.");
    }
  }
}
