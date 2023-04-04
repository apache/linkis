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

package org.apache.linkis.storage.script.parser;

import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.script.Parser;
import org.apache.linkis.storage.script.Variable;
import org.apache.linkis.storage.script.VariableParser;

public abstract class CommonScriptParser implements Parser {

  @Override
  public Variable parse(String line) {
    String variableReg = "\\s*" + prefix() + "\\s*(.+)\\s*=\\s*(.+)\\s*";
    if (line.matches(variableReg)) {
      String[] split = line.split("=");
      String key = split[0].trim();
      String value = split[1].trim();
      return new Variable(VariableParser.VARIABLE, null, key, value);
    } else {
      String[] split = line.split(" ");
      if (split.length != 4) {
        throw new StorageWarnException(
            LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER.getErrorCode(),
            LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER.getErrorDesc());
      }
      String prefixConf = prefixConf();
      if (!split[0].trim().equals(prefixConf)) {
        throw new StorageWarnException(
            LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER.getErrorCode(),
            LinkisStorageErrorCodeSummary.INVALID_CUSTOM_PARAMETER.getErrorDesc());
      }
      String sortParent = split[1].trim();
      String sort = split[2].trim();
      String key = split[3].trim();
      String value = line.substring(line.indexOf("=") + 1).trim();
      return new Variable(sortParent, sort, key, value);
    }
  }

  @Override
  public String getAnnotationSymbol() {
    return prefix().split("@")[0];
  }
}
