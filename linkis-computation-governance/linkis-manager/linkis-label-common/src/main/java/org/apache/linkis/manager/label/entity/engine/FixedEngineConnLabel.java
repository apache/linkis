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

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.*;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary.CHECK_LABEL_VALUE_EMPTY;
import static org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary.LABEL_ERROR_CODE;

public class FixedEngineConnLabel extends GenericLabel implements EngineNodeLabel, UserModifiable {

  public FixedEngineConnLabel() {
    setLabelKey(LabelKeyConstant.FIXED_EC_KEY);
  }

  @ValueSerialNum(0)
  public void setSessionId(String SessionId) {
    if (getValue() == null) {
      setValue(new HashMap<>());
    }
    getValue().put("sessionId", SessionId);
  }

  public String getSessionId() {
    if (getValue() == null) {
      return null;
    }
    return getValue().get("sessionId");
  }

  @Override
  public Feature getFeature() {
    return Feature.CORE;
  }

  @Override
  public void valueCheck(String stringValue) throws LabelErrorException {
    if (!StringUtils.isBlank(stringValue)) {
      if (stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 1) {
        throw new LabelErrorException(
            LABEL_ERROR_CODE.getErrorCode(), LABEL_ERROR_CODE.getErrorDesc());
      }
    } else {
      throw new LabelErrorException(
          CHECK_LABEL_VALUE_EMPTY.getErrorCode(), CHECK_LABEL_VALUE_EMPTY.getErrorDesc());
    }
  }
}
