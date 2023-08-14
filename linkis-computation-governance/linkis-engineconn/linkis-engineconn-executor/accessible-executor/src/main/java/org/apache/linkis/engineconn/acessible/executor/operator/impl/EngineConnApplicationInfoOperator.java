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

package org.apache.linkis.engineconn.acessible.executor.operator.impl;

import org.apache.linkis.engineconn.common.exception.EngineConnException;
import org.apache.linkis.engineconn.core.executor.ExecutorManager$;
import org.apache.linkis.engineconn.core.executor.LabelExecutorManager;
import org.apache.linkis.engineconn.executor.entity.Executor;
import org.apache.linkis.engineconn.executor.entity.YarnExecutor;
import org.apache.linkis.governance.common.constant.ec.ECConstants;
import org.apache.linkis.manager.common.operator.Operator;

import java.util.HashMap;
import java.util.Map;

public class EngineConnApplicationInfoOperator implements Operator {

  public static final String OPERATOR_NAME = "engineConnYarnApplication";

  @Override
  public String[] getNames() {
    return new String[] {OPERATOR_NAME};
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> parameters) {
    LabelExecutorManager instance = ExecutorManager$.MODULE$.getInstance();
    Executor reportExecutor = instance.getReportExecutor();
    if (reportExecutor instanceof YarnExecutor) {
      YarnExecutor yarnExecutor = (YarnExecutor) reportExecutor;
      Map<String, Object> result = new HashMap<>();
      result.put(ECConstants.YARN_APPID_NAME_KEY(), yarnExecutor.getApplicationId());
      result.put(ECConstants.YARN_APP_URL_KEY(), yarnExecutor.getApplicationURL());
      result.put(ECConstants.QUEUE(), yarnExecutor.getQueue());
      result.put(ECConstants.YARN_MODE_KEY(), yarnExecutor.getYarnMode());
      return result;
    } else {
      throw new EngineConnException(
          20301, "EngineConn is not a yarn application, cannot fetch applicaiton info.");
    }
  }
}
