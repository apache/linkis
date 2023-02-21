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

package org.apache.linkis.engineplugin.vo;

public class EnginePluginBMLVo extends PageViewVo {
  private String engineConnType;
  private String engineConnVersion;

  public EnginePluginBMLVo(String engineConnType, String engineConnVersion) {
    this.engineConnType = engineConnType;
    this.engineConnVersion = engineConnVersion;
  }

  public String getEngineConnType() {
    return engineConnType;
  }

  public void setEngineConnType(String engineConnType) {
    this.engineConnType = engineConnType;
  }

  public String getEngineConnVersion() {
    return engineConnVersion;
  }

  public void setEngineConnVersion(String engineConnVersion) {
    this.engineConnVersion = engineConnVersion;
  }
}
