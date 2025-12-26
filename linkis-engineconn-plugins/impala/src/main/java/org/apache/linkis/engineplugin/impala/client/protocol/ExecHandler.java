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

package org.apache.linkis.engineplugin.impala.client.protocol;

import org.apache.linkis.engineplugin.impala.client.ExecutionListener;

public abstract class ExecHandler<T> implements AutoCloseable {
  private String queryId;
  private T handler;
  private ExecutionListener executionListener = null;
  private int errors;

  private boolean isQueued;

  public ExecHandler() {}

  public ExecHandler(String queryId, T handler, ExecutionListener executionListener) {
    this.queryId = queryId;
    this.handler = handler;

    if (executionListener != null) {
      this.executionListener = executionListener;
      executionListener.created(queryId);
    }

    this.errors = 0;
    this.isQueued = false;
  }

  public int markError() {
    return ++errors;
  }

  public String getQueryId() {
    return queryId;
  }

  public T getHandler() {
    return handler;
  }

  public ExecutionListener getResultListener() {
    return executionListener;
  }

  public int getErrors() {
    return errors;
  }

  public boolean isQueued() {
    return isQueued;
  }

  public void setQueued(boolean queued) {
    isQueued = queued;
  }
}
