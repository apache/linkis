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

package org.apache.linkis.manager.engineplugin.python.utils;

import org.apache.commons.lang3.StringUtils;

public abstract class Kind {

  public static final String RESTART_CODE = "@restart";

  public static final String[] APPLICATION_START_COMMAND =
      new String[] {
        "\\s*@restart\\s*",
        "\\s*[@|%][a-zA-Z]{1,12}\\s*",
        "^\\s*@set\\s*spark\\..+\\s*",
        "^\\s*#.+\\s*",
        "^\\s*//.+\\s*",
        "^\\s*--.+\\s*",
        "\\s*"
      };

  public static boolean needToRestart(String code) {
    return code.startsWith(RESTART_CODE);
  }

  private static int getIndex(String _code, int start) {
    int index1 = _code.indexOf("\n", start);
    int index2 = _code.indexOf("\\n", start);
    if (index1 > -1 && index2 > -1) {
      return Math.min(index1, index2);
    } else {
      return Math.max(index1, index2);
    }
  }

  public static String getKindString(String code) {
    code = StringUtils.strip(code);

    int start = 0;
    if (code.startsWith(RESTART_CODE)) {
      start = getIndex(code, 0) + 1;
    }
    int index = getIndex(code, start);
    if (index == -1) {
      index = code.length();
    }
    return StringUtils.strip(code.substring(start, index));
  }

  public static String getKind(String code) {
    String kindStr = getKindString(code);
    if (kindStr.matches("[%|@][a-zA-Z]{1,12}")) {
      return kindStr.substring(1);
    } else {
      throw new IllegalArgumentException("Unknown kind " + kindStr);
    }
  }

  /**
   * This method just removes @restart and language identifiers (such as %sql, %scala, etc.), that
   * is, removes at most the first 2 rows. 该方法只是去掉了@restart和语言标识符（如%sql、%scala等），即最多只去掉最前面2行
   *
   * @param code
   * @return
   */
  public static String getRealCode(String code) {
    code = StringUtils.strip(code);
    String kindStr = getKindString(code);
    if (kindStr.matches("[%|@][a-zA-Z]{1,12}")) {
      return StringUtils.strip(code.substring(code.indexOf(kindStr) + kindStr.length()));
    } else if (code.startsWith(RESTART_CODE)) {
      return StringUtils.strip(code.substring(RESTART_CODE.length()));
    } else {
      return code;
    }
  }
}
