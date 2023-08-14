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

package org.apache.linkis.manager.common.utils;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.manager.common.conf.ManagerCommonConf;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactory;
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext;
import org.apache.linkis.manager.label.entity.Label;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class ManagerUtils {

  private static final LabelBuilderFactory labelFactory =
      LabelBuilderFactoryContext.getLabelBuilderFactory();

  public static <T> T getValueOrDefault(Map<String, Object> map, String key, T defaultValue) {
    Object value = defaultValue;
    if (null != map && null != map.get(key)) {
      value = map.get(key);
    }
    return (T) value;
  }

  public static String getAdminUser() {

    if (StringUtils.isNotBlank(ManagerCommonConf.DEFAULT_ADMIN.getValue())) {
      return ManagerCommonConf.DEFAULT_ADMIN.getValue();
    }
    return Utils.getJvmUser();
  }

  public static Label<?> persistenceLabelToRealLabel(Label<?> label) {
    return labelFactory.createLabel(label.getLabelKey(), label.getValue());
  }
}
