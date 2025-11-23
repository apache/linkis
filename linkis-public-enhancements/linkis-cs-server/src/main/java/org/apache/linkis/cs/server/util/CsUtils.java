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

package org.apache.linkis.cs.server.util;

import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import org.apache.linkis.cs.common.serialize.helper.SerializationHelper;

public class CsUtils {

  public static final SerializationHelper SERIALIZE = ContextSerializationHelper.getInstance();

  public static String serialize(Object o) throws CSErrorException {
    if (o instanceof String) {
      return (String) o;
    }
    if (o instanceof Integer) {
      return String.valueOf((int) o);
    }
    return SERIALIZE.serialize(o);
  }
}
