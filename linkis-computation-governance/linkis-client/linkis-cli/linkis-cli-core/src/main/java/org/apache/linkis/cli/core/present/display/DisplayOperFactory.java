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

package org.apache.linkis.cli.core.present.display;

import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.present.PresentMode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DisplayOperFactory {
  private static final Map<String, DisplayOperator> operatorMap = new ConcurrentHashMap<>();

  public static synchronized void register(PresentMode mode, DisplayOperator operator)
      throws Exception {
    if (operatorMap.containsKey(mode.getName())) {
      throw new PresenterException(
          "PST0012",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterInitErr,
          "Attempting to register a duplicate DisplayOperator, name: " + mode.getName());
    }
    operatorMap.put(mode.getName(), operator);
  }

  public static synchronized void remove(PresentMode mode) {
    operatorMap.remove(mode.getName());
  }

  public static DisplayOperator getDisplayOper(PresentMode mode) {
    return operatorMap.get(mode.getName());
  }
}
