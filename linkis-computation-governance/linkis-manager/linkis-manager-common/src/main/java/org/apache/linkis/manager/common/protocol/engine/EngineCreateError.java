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

package org.apache.linkis.manager.common.protocol.engine;

public class EngineCreateError implements EngineAsyncResponse {

  private String id;

  private String exception;

  private Boolean retry;

  public EngineCreateError(String id, String exception, Boolean retry) {
    this.id = id;
    this.exception = exception;
    this.retry = retry;
  }

  public EngineCreateError(String id, String exception) {
    this(id, exception, false);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getException() {
    return exception;
  }

  public void setException(String exception) {
    this.exception = exception;
  }

  public Boolean getRetry() {
    return retry;
  }

  public void setRetry(Boolean retry) {
    this.retry = retry;
  }

  @Override
  public String toString() {
    return "EngineCreateError{" + "id='" + id + '\'' + ", retry=" + retry + '}';
  }
}
