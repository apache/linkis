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

package org.apache.linkis.common.exception;

import org.apache.linkis.common.errorcode.CommonErrorConstants;

import java.util.HashMap;
import java.util.Map;

import static org.apache.linkis.common.exception.ExceptionLevel.*;

public final class ExceptionManager {
  private static final String LEVEL = "level";
  private static final String ERRCODE = "errCode";
  private static final String DESC = "desc";
  private static final String IP = "ip";
  private static final String PORT = "port";
  private static final String SERVICEKIND = "serviceKind";

  public static Exception generateException(Map<String, Object> map) {
    Exception retException = null;
    if (null == map || map.get(LEVEL) == null) {
      return new ErrorException(
          CommonErrorConstants.COMMON_ERROR(),
          "The map cannot be parsed normally, "
              + "the map is empty or the LEVEL value is missing:(map不能被正常的解析，map为空或者缺少LEVEL值: )"
              + map);
    }
    int level = Integer.parseInt(map.get(LEVEL).toString());
    int errCode = Integer.parseInt(map.get(ERRCODE).toString());
    String desc = (String) map.get(DESC);
    String ip = (String) map.get(IP);
    int port = Integer.parseInt(map.get(PORT).toString());
    String serviceKind = (String) map.get(SERVICEKIND);
    if (WARN.getLevel() == level) {
      retException = new WarnException(errCode, desc, ip, port, serviceKind);
    } else if (ERROR.getLevel() == level) {
      retException = new ErrorException(errCode, desc, ip, port, serviceKind);
    } else if (FATAL.getLevel() == level) {
      retException = new FatalException(errCode, desc, ip, port, serviceKind);
    } else if (RETRY.getLevel() == level) {
      retException = new LinkisRetryException(errCode, desc, ip, port, serviceKind);
    }
    return retException != null
        ? retException
        : new ErrorException(
            CommonErrorConstants.COMMON_ERROR(),
            "Exception Map that cannot be parsed:(不能解析的异常Map：)" + map);
  }

  public static Map<String, Object> unknownException(String errorMsg) {
    Map<String, Object> retMap = new HashMap<String, Object>();
    retMap.put("level", ERROR.getLevel());
    retMap.put("errCode", 0);
    retMap.put("desc", errorMsg);
    retMap.put("ip", LinkisException.hostname);
    retMap.put("port", LinkisException.hostPort);
    retMap.put("serviceKind", LinkisException.applicationName);
    return retMap;
  }
}
