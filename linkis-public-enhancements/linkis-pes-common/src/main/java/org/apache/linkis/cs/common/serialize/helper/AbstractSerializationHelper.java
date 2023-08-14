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

package org.apache.linkis.cs.common.serialize.helper;

import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.ContextSerializer;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class AbstractSerializationHelper implements SerializationHelper {

  protected abstract Map<String, ContextSerializer> getContextSerializerMap();

  @Override
  public boolean accepts(String json) {
    return null != getContextSerializer(json);
  }

  @Override
  public boolean accepts(Object obj) {
    return null != getContextSerializer(obj);
  }

  @Override
  public String serialize(Object obj) throws CSErrorException {
    ContextSerializer contextSerializer = getContextSerializer(obj);
    if (null != contextSerializer) {
      return contextSerializer.serialize(obj);
    }

    if (null != obj) {
      throw new CSErrorException(97001, "Failed to find Serializer of " + obj.getClass().getName());
    }
    throw new CSErrorException(97001, "The obj not null");
  }

  @Override
  public Object deserialize(String json) throws CSErrorException {

    ContextSerializer contextSerializer = getContextSerializer(json);

    if (contextSerializer != null) {
      return contextSerializer.deserialize(json);
    }
    if (StringUtils.isNotBlank(json)) {
      throw new CSErrorException(97001, "Failed to find deserialize of " + json);
    }
    throw new CSErrorException(97001, "The json not null");
  }

  @Override
  public <T> T deserialize(String s, Class<T> interfaceClass) throws CSErrorException {
    return null;
  }

  private ContextSerializer getContextSerializer(String json) {

    if (StringUtils.isNotBlank(json)) {
      Map<String, String> value =
          CSCommonUtils.gson.fromJson(json, new HashMap<String, String>().getClass());
      String type = value.get("type");
      return getContextSerializerMap().get(type);
    }
    return null;
  }

  private ContextSerializer getContextSerializer(Object obj) {

    if (null != obj) {
      Stream<ContextSerializer> contextSerializerStream =
          getContextSerializerMap().values().stream()
              .filter(contextSerializer -> contextSerializer.accepts(obj));
      if (null != contextSerializerStream) {
        Optional<ContextSerializer> first = contextSerializerStream.findFirst();
        if (first.isPresent()) {
          return first.get();
        }
      }
    }
    return null;
  }
}
