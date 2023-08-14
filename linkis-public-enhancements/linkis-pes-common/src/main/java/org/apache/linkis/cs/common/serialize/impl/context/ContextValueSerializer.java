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

package org.apache.linkis.cs.common.serialize.impl.context;

import org.apache.linkis.cs.common.entity.source.CommonContextValue;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.AbstractSerializer;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextValueSerializer extends AbstractSerializer<CommonContextValue> {

  private static final Logger logger = LoggerFactory.getLogger(ContextValueSerializer.class);

  @Override
  public String getType() {
    return "contextValue";
  }

  @Override
  public String getJsonValue(CommonContextValue commonContextValue) throws CSErrorException {
    Object value = commonContextValue.getValue();
    try {
      Map<String, String> map = new HashMap<>();
      String keywords = commonContextValue.getKeywords();
      String json = ContextSerializationHelper.getInstance().serialize(value);
      map.put("keywords", keywords);
      map.put("value", json);
      return CSCommonUtils.gson.toJson(map);
    } catch (Exception e) {
      logger.error("Failed to serialize contextValue: ", e);
      throw new CSErrorException(97000, "Failed to serialize contextValue");
    }
  }

  @Override
  public CommonContextValue fromJson(String json) throws CSErrorException {
    try {
      Map<String, String> jsonObj =
          CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
      String value = jsonObj.get("value");
      String keywords = jsonObj.get("keywords");
      Object valueObj = ContextSerializationHelper.getInstance().deserialize(value);
      CommonContextValue commonContextValue = new CommonContextValue();
      commonContextValue.setKeywords(keywords);
      commonContextValue.setValue(valueObj);
      return commonContextValue;
    } catch (Exception e) {
      logger.error("Failed to deserialize contextValue: ", e);
      throw new CSErrorException(97000, "Failed to serialize contextValue");
    }
  }

  @Override
  public boolean accepts(Object obj) {
    if (null != obj && obj instanceof CommonContextValue) {
      return true;
    }
    return false;
  }
}
