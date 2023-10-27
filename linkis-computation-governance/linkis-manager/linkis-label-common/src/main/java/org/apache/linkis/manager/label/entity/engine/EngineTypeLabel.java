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

package org.apache.linkis.manager.label.entity.engine;

import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.EMNodeLabel;
import org.apache.linkis.manager.label.entity.EngineNodeLabel;
import org.apache.linkis.manager.label.entity.Feature;
import org.apache.linkis.manager.label.entity.GenericLabel;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import com.fasterxml.jackson.core.JsonProcessingException;

public class EngineTypeLabel extends GenericLabel implements EngineNodeLabel, EMNodeLabel {

  public EngineTypeLabel() {
    setLabelKey(LabelKeyConstant.ENGINE_TYPE_KEY);
  }

  @Override
  public Feature getFeature() {
    return Feature.CORE;
  }

  public String getEngineType() {
    if (null == getValue()) {
      return null;
    }
    return getValue().get("engineType");
  }

  public String getVersion() {
    if (null == getValue()) {
      return null;
    }

    return getValue().get("version");
  }

  @ValueSerialNum(0)
  public void setEngineType(String type) {
    if (null == getValue()) {
      setValue(new HashMap<>());
    }
    getValue().put("engineType", type);
  }

  @ValueSerialNum(1)
  public void setVersion(String version) {
    if (null == getValue()) {
      setValue(new HashMap<>());
    }
    getValue().put("version", version);
  }

  @Override
  public Boolean isEmpty() {
    return StringUtils.isBlank(getEngineType()) || StringUtils.isBlank(getVersion());
  }

  @Override
  public void setStringValue(String stringValue) {
    if (StringUtils.isNotBlank(stringValue)) {
      try {
        HashMap<String, String> valueMap =
            JsonUtils.jackson().readValue(stringValue, HashMap.class);
        setEngineType(valueMap.get("engineType"));
        setVersion(valueMap.get("version"));
      } catch (JsonProcessingException e) {
        String version;
        String engineType = stringValue.split("-")[0];

        if (engineType.equals("*")) {
          version = stringValue.replaceFirst("[" + engineType + "]-", "");
        } else {
          version = stringValue.replaceFirst(engineType + "-", "");
        }

        setEngineType(engineType);
        setVersion(version);
      }
    } else {
      setEngineType("*");
      setVersion("*");
    }
  }
}
