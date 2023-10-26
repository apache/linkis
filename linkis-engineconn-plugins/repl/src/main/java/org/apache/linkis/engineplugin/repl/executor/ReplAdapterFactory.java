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

package org.apache.linkis.engineplugin.repl.executor;

import org.apache.linkis.engineplugin.repl.conf.ReplType;
import org.apache.linkis.engineplugin.repl.errorcode.ReplErrorCodeSummary;
import org.apache.linkis.engineplugin.repl.exception.ReplException;

import org.apache.commons.lang3.StringUtils;

public class ReplAdapterFactory {

  public static ReplAdapter create(String replType) {
    if (StringUtils.isBlank(replType) || !ReplType.isSupportReplType(replType)) {
      throw new ReplException(
          ReplErrorCodeSummary.NOT_SUPPORT_REPL_TYPE.getErrorCode(),
          ReplErrorCodeSummary.NOT_SUPPORT_REPL_TYPE.getErrorDesc());
    }

    ReplAdapter replAdapter = null;

    if (ReplType.JAVA.equalsIgnoreCase(replType)) {
      replAdapter = new JavaReplAdapter();
    } else if (ReplType.SCALA.equalsIgnoreCase(replType)) {
      replAdapter = new ScalaReplAdapter();
    }
    return replAdapter;
  }
}
