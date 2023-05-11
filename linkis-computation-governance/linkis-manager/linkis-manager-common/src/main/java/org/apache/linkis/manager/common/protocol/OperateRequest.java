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

package org.apache.linkis.manager.common.protocol;

import org.apache.linkis.governance.common.exception.GovernanceErrorException;

import java.util.Map;

public interface OperateRequest {
  String getUser();

  Map<String, Object> getParameters();

  String OPERATOR_NAME_KEY = "__operator_name__";

  static String getOperationName(Map<String, Object> parameters) {
    Object obj = parameters.get(OPERATOR_NAME_KEY);
    if (obj instanceof String) {
      return (String) obj;
    } else {
      throw new GovernanceErrorException(20031, OPERATOR_NAME_KEY + " does not exist.");
    }
  }
}
