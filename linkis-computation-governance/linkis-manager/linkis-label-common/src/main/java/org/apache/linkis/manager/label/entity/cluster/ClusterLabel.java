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

package org.apache.linkis.manager.label.entity.cluster;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.manager.label.entity.*;
import org.apache.linkis.manager.label.entity.annon.ValueSerialNum;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

import static org.apache.linkis.manager.label.errorcode.LabelCommonErrorCodeSummary.CLUSTER_LABEL_VALUE_ERROR;

public class ClusterLabel extends GenericLabel implements EngineNodeLabel, UserModifiable {

  public ClusterLabel() {
    setLabelKey(LabelKeyConstant.YARN_CLUSTER_KEY);
  }

  @Override
  public Feature getFeature() {
    return Feature.CORE;
  }

  @ValueSerialNum(1)
  public void setClusterName(String clusterName) {
    if (null == getValue()) {
      setValue(new HashMap<>());
    }
    getValue().put("clusterName", clusterName);
  }

  public String getClusterName() {
    if (null != getValue().get("clusterName")) {
      return getValue().get("clusterName");
    }
    return null;
  }

  @ValueSerialNum(0)
  public void setClusterType(String clusterType) {
    if (null == getValue()) {
      setValue(new HashMap<>());
    }
    getValue().put("clusterType", clusterType);
  }

  public String getClusterType() {
    if (null != getValue().get("clusterType")) {
      return getValue().get("clusterType");
    }
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof ClusterLabel) {
      if (null != getClusterName() && null != getClusterType()) {
        return getClusterName().equals(((ClusterLabel) other).getClusterName())
            && getClusterType().equals(((ClusterLabel) other).getClusterType());
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public void valueCheck(String stringValue) throws LabelErrorException {
    if (!StringUtils.isEmpty(stringValue)) {
      if (stringValue.split(SerializableLabel.VALUE_SEPARATOR).length != 2) {
        throw new LabelErrorException(
            CLUSTER_LABEL_VALUE_ERROR.getErrorCode(), CLUSTER_LABEL_VALUE_ERROR.getErrorDesc());
      }
    }
  }
}
