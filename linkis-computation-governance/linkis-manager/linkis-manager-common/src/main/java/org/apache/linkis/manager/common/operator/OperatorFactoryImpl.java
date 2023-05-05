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

package org.apache.linkis.manager.common.operator;

import org.apache.linkis.common.utils.ClassUtils;
import org.apache.linkis.governance.common.exception.GovernanceErrorException;
import org.apache.linkis.manager.common.protocol.OperateRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class OperatorFactoryImpl implements OperatorFactory {

  private static final Logger logger = LoggerFactory.getLogger(OperatorFactoryImpl.class);

  private final Map<String, ? extends Operator> operators;

  OperatorFactoryImpl() {
    Set<Class<? extends Operator>> subTypes =
        ClassUtils.reflections().getSubTypesOf(Operator.class);
    Map<String, Operator> operatorMap = new HashMap<>();
    for (Class<? extends Operator> clazz : subTypes) {
      if (!ClassUtils.isInterfaceOrAbstract(clazz)) {
        Operator operator = null;
        try {
          operator = clazz.newInstance();
          for (String name : operator.getNames()) {
            operatorMap.put(name, operator);
          }
        } catch (InstantiationException | IllegalAccessException e) {
          logger.error("Failed to create operator instance for class: " + clazz.getName(), e);
        }
      }
    }
    operators = operatorMap;
    logger.info("Launched operators list => " + operators);
  }

  @Override
  public String getOperatorName(Map<String, Object> parameters) {
    return OperateRequest.getOperationName(parameters);
  }

  @Override
  public Operator getOperatorRequest(Map<String, Object> parameters) {
    String operatorName = getOperatorName(parameters);
    if (operators.containsKey(operatorName)) {
      return operators.get(operatorName);
    } else {
      throw new GovernanceErrorException(20030, "Cannot find operator named " + operatorName + ".");
    }
  }
}
