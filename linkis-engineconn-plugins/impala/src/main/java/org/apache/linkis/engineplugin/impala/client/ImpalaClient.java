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

package org.apache.linkis.engineplugin.impala.client;

import org.apache.linkis.engineplugin.impala.client.exception.ImpalaEngineException;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecProgress;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecStatus;
import org.apache.linkis.engineplugin.impala.client.protocol.ExecSummary;

import java.util.Map;

public interface ImpalaClient extends AutoCloseable {

  /**
   * execute async with listener
   *
   * @return queryId
   */
  default String executeAsync(String sql, ExecutionListener executionListener)
      throws ImpalaEngineException {
    return executeAsync(sql, executionListener, null);
  }

  /**
   * execute async with listener and options
   *
   * @return queryId
   */
  String executeAsync(
      String sql, ExecutionListener executionListener, Map<String, String> queryOptions)
      throws ImpalaEngineException;

  /** execute with listener */
  default void execute(String sql, ExecutionListener executionListener)
      throws ImpalaEngineException, InterruptedException {
    execute(sql, executionListener, null);
  }

  /** execute with listener and options */
  void execute(String sql, ExecutionListener executionListener, Map<String, String> queryOptions)
      throws ImpalaEngineException, InterruptedException;

  /** cancel execution */
  void cancel(String queryId) throws ImpalaEngineException;

  /** get execution summary */
  ExecSummary getExecSummary(String queryId) throws ImpalaEngineException;

  /** get execution summary progress */
  ExecProgress getExecProgress(String queryId) throws ImpalaEngineException;

  /** get execution status */
  ExecStatus getExecStatus(String queryId) throws ImpalaEngineException;

  void setQueryOption(String key, String value) throws ImpalaEngineException;

  Map<String, String> getQueryOptions() throws ImpalaEngineException;

  void unsetQueryOption(String key) throws ImpalaEngineException;

  int getRunningExecutionCount();
}
