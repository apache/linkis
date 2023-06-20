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

import org.apache.hive.service.rpc.thrift.TOperationState;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class ExecStatus {
  private static final Set<Integer> ACTIVE_STATE =
      ImmutableSet.of(
          TOperationState.INITIALIZED_STATE.getValue(),
          TOperationState.RUNNING_STATE.getValue(),
          TOperationState.PENDING_STATE.getValue());

  private static final Set<Integer> ERROR_STATE =
      ImmutableSet.of(
          TOperationState.ERROR_STATE.getValue(),
          TOperationState.CLOSED_STATE.getValue(),
          TOperationState.CANCELED_STATE.getValue(),
          TOperationState.UKNOWN_STATE.getValue());

  private int code;
  private String name;
  private String errorMessage;

  public ExecStatus() {}

  public ExecStatus(int code, String name, String errorMessage) {
    super();
    this.code = code;
    this.name = name;
    this.errorMessage = errorMessage;
  }

  public int getCode() {
    return code;
  }

  public String getName() {
    return name;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public boolean isActive() {
    return ACTIVE_STATE.contains(code);
  }

  public boolean hasError() {
    return ERROR_STATE.contains(code);
  }
}
