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

package org.apache.linkis.engineplugin.impala.client.util;

import org.apache.linkis.engineplugin.impala.client.ExecutionListener;
import org.apache.linkis.engineplugin.impala.client.exception.ImpalaEngineException;
import org.apache.linkis.engineplugin.impala.client.exception.ImpalaErrorCodeSummary;

import org.apache.hive.service.rpc.thrift.TStatus;

public class ThriftUtil {
  private static final char[] hexCode = "0123456789abcdef".toCharArray();

  public static void checkStatus(TStatus status, ExecutionListener executionListener)
      throws ImpalaEngineException {
    switch (status.getStatusCode()) {
      case STILL_EXECUTING_STATUS:
        throw ImpalaEngineException.of(ImpalaErrorCodeSummary.StillRunningError);
      case ERROR_STATUS:
        throw ImpalaEngineException.of(
            ImpalaErrorCodeSummary.ExecutionError, status.getErrorMessage());
      case INVALID_HANDLE_STATUS:
        throw ImpalaEngineException.of(ImpalaErrorCodeSummary.InvalidHandleError);
      case SUCCESS_WITH_INFO_STATUS:
        if (executionListener != null) {
          executionListener.message(status.getInfoMessages());
        }
        break;
      case SUCCESS_STATUS:
    }
  }

  public static void checkStatus(TStatus status) throws ImpalaEngineException {
    checkStatus(status, null);
  }

  /*
   * impala unique id
   */
  public static String convertUniqueId(byte[] b) {
    StringBuilder sb = new StringBuilder(":");
    for (int i = 0; i < 8; ++i) {
      sb.append(hexCode[(b[15 - i] >> 4) & 0xF]);
      sb.append(hexCode[(b[15 - i] & 0xF)]);
      sb.insert(0, hexCode[(b[i] & 0xF)]);
      sb.insert(0, hexCode[(b[i] >> 4) & 0xF]);
    }
    return sb.toString();
  }
}
