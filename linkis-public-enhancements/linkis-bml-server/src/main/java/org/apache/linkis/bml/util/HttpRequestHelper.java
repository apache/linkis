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

package org.apache.linkis.bml.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestHelper {

  private static final String UN_KNOWN = "unKnown";

  public static String getIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");
    if (StringUtils.isNotEmpty(ip) && !UN_KNOWN.equalsIgnoreCase(ip)) {
      // 多次反向代理后会有多个ip值，第一个ip才是真实ip
      int index = ip.indexOf(",");
      if (index != -1) {
        return ip.substring(0, index);
      } else {
        return ip;
      }
    }
    ip = request.getHeader("X-Real-IP");
    if (StringUtils.isNotEmpty(ip) && !UN_KNOWN.equalsIgnoreCase(ip)) {
      return ip;
    }
    return request.getRemoteAddr();
  }
}
