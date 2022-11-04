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

package org.apache.linkis.metadata.domain.mdq.bo;

public class ModelBO {
  private Integer lifecycle;
  private Integer modelLevel;
  private Integer useWay;
  private Boolean isExternalUse;

  public Integer getLifecycle() {
    return lifecycle;
  }

  public void setLifecycle(Integer lifecycle) {
    this.lifecycle = lifecycle;
  }

  public Integer getModelLevel() {
    return modelLevel;
  }

  public void setModelLevel(Integer modelLevel) {
    this.modelLevel = modelLevel;
  }

  public Integer getUseWay() {
    return useWay;
  }

  public void setUseWay(Integer useWay) {
    this.useWay = useWay;
  }

  public Boolean getExternalUse() {
    return isExternalUse;
  }

  public void setExternalUse(Boolean externalUse) {
    isExternalUse = externalUse;
  }
}
