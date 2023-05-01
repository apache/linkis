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

import org.apache.linkis.common.ServiceInstance;

public class RequestManagerUnlock implements EngineLock {
  private ServiceInstance engineInstance;
  private String lock;
  private ServiceInstance clientInstance;

  public RequestManagerUnlock() {}

  public RequestManagerUnlock(
      ServiceInstance engineInstance, String lock, ServiceInstance clientInstance) {
    this.engineInstance = engineInstance;
    this.lock = lock;
    this.clientInstance = clientInstance;
  }

  public ServiceInstance getEngineInstance() {
    return engineInstance;
  }

  public void setEngineInstance(ServiceInstance engineInstance) {
    this.engineInstance = engineInstance;
  }

  public String getLock() {
    return lock;
  }

  public void setLock(String lock) {
    this.lock = lock;
  }

  public ServiceInstance getClientInstance() {
    return clientInstance;
  }

  public void setClientInstance(ServiceInstance clientInstance) {
    this.clientInstance = clientInstance;
  }
}
