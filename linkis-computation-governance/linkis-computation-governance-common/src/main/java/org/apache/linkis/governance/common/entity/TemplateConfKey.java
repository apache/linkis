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

package org.apache.linkis.governance.common.entity;

public class TemplateConfKey {

  private String templateUuid;

  private String key;

  private String templateName;

  private String configValue;

  public String getTemplateUuid() {
    return templateUuid;
  }

  public void setTemplateUuid(String templateUuid) {
    this.templateUuid = templateUuid;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  @Override
  public String toString() {
    return "TemplateKey{"
        + "templateUuid='"
        + templateUuid
        + '\''
        + ", key='"
        + key
        + '\''
        + ", templateName='"
        + templateName
        + '\''
        + ", configValue='"
        + configValue
        + '\''
        + '}';
  }
}
