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

package org.apache.linkis.engineplugin.trino.utils;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.engineplugin.trino.conf.TrinoConfiguration;
import org.apache.linkis.engineplugin.trino.exception.TrinoGrantmaException;
import org.apache.linkis.engineplugin.trino.exception.TrinoModifySchemaException;

public class TrinoCode {
  private static final String SPACE = " ";

  private static boolean willGrant(String code) {
    return code.matches("(?=.*grant)(?=.*on)(?=.*to)^.*$");
  }

  private static boolean willModifySchema(String code) {
    String trimmedCode = code.replaceAll("\n", SPACE).replaceAll("\\s+", SPACE).toLowerCase();
    return trimmedCode.contains("create schema")
        || trimmedCode.contains("drop schema")
        || trimmedCode.contains("alter schema");
  }

  public static void checkCode(String code) throws ErrorException {
    if (TrinoConfiguration.TRINO_FORBID_MODIFY_SCHEMA.getValue() && willModifySchema(code)) {
      throw new TrinoModifySchemaException("CREATE, ALTER, DROP SCHEMA is not allowed");
    }
    if (TrinoConfiguration.TRINO_FORBID_GRANT.getValue() && willGrant(code)) {
      throw new TrinoGrantmaException("Grant schema or table is not allowed");
    }
  }
}
