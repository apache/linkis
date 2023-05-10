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

package org.apache.linkis.engineconn.once.executor.operator;

import org.apache.linkis.engineconn.common.exception.EngineConnException;
import org.apache.linkis.engineconn.once.executor.OnceExecutor;
import org.apache.linkis.engineconn.once.executor.OperableOnceExecutor;
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorManager$;
import org.apache.linkis.manager.common.operator.Operator;
import org.apache.linkis.manager.common.operator.OperatorFactory;
import org.apache.linkis.protocol.engine.JobProgressInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperableOnceEngineConnOperator implements Operator {

  public static final String PROGRESS_OPERATOR_NAME = "engineConnProgress";

  public static final String METRICS_OPERATOR_NAME = "engineConnMetrics";

  public static final String DIAGNOSIS_OPERATOR_NAME = "engineConnDiagnosis";

  @Override
  public String[] getNames() {
    return new String[] {PROGRESS_OPERATOR_NAME, METRICS_OPERATOR_NAME, DIAGNOSIS_OPERATOR_NAME};
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> parameters) {
    String operatorName = OperatorFactory.apply().getOperatorName(parameters);
    OnceExecutor reportExecutor = OnceExecutorManager$.MODULE$.getInstance().getReportExecutor();
    if (reportExecutor instanceof OperableOnceExecutor) {
      OperableOnceExecutor operableOnceExecutor = (OperableOnceExecutor) reportExecutor;
      switch (operatorName) {
        case PROGRESS_OPERATOR_NAME:
          List<Map<String, Object>> progressInfoMap = new ArrayList<>();
          JobProgressInfo[] progressInfoList = operableOnceExecutor.getProgressInfo();
          if (progressInfoList != null && progressInfoList.length != 0) {
            for (JobProgressInfo progressInfo : progressInfoList) {
              Map<String, Object> infoMap = new HashMap<>();
              infoMap.put("id", progressInfo.id());
              infoMap.put("totalTasks", progressInfo.totalTasks());
              infoMap.put("runningTasks", progressInfo.runningTasks());
              infoMap.put("failedTasks", progressInfo.failedTasks());
              infoMap.put("succeedTasks", progressInfo.succeedTasks());
              progressInfoMap.add(infoMap);
            }
          }
          Map<String, Object> resultMap = new HashMap<>();
          resultMap.put("progress", operableOnceExecutor.getProgress());
          resultMap.put("progressInfo", progressInfoMap);
          return resultMap;
        case METRICS_OPERATOR_NAME:
          return new HashMap<String, Object>() {
            {
              put("metrics", operableOnceExecutor.getMetrics());
            }
          };
        case DIAGNOSIS_OPERATOR_NAME:
          return new HashMap<String, Object>() {
            {
              put("diagnosis", operableOnceExecutor.getDiagnosis());
            }
          };
        default:
          throw new EngineConnException(
              20308, "This engineConn don't support " + operatorName + " operator.");
      }
    } else {
      throw new EngineConnException(
          20308, "This engineConn don't support " + operatorName + " operator.");
    }
  }
}
