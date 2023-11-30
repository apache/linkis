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
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.common.exception.ExceptionLevel.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionManagerTest {

  @Test
  void testGenerateException() {
    ErrorException errorException =
        new ErrorException(
            CommonErrorConstants.COMMON_ERROR(),
            "The map cannot be parsed normally, "
                + "the map is empty or the LEVEL value is missing:(map不能被正常的解析，map为空或者缺少LEVEL值: )"
                + "null");
    assertEquals(errorException.getClass(), ExceptionManager.generateException(null).getClass());
    assertEquals(errorException.toString(), ExceptionManager.generateException(null).toString());
    Map<String, Object> map = new TreeMap<>();
    map.put("level", null);
    map.put("errCode", 1);
    map.put("desc", "test");
    map.put("ip", LinkisException.hostname);
    map.put("port", LinkisException.hostPort);
    map.put("serviceKind", LinkisException.applicationName);
    errorException.setDesc(
        "The map cannot be parsed normally, "
            + "the map is empty or the LEVEL value is missing:(map不能被正常的解析，map为空或者缺少LEVEL值: )"
            + map);
    assertEquals(errorException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(errorException.toString(), ExceptionManager.generateException(map).toString());
    map.replace("level", ERROR.getLevel());
    errorException.setErrCode((Integer) map.get("errCode"));
    errorException.setIp(LinkisException.hostname);
    errorException.setPort(LinkisException.hostPort);
    errorException.setServiceKind(LinkisException.applicationName);
    errorException.setDesc((String) map.get("desc"));
    assertEquals(errorException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(errorException.toString(), ExceptionManager.generateException(map).toString());
    map.replace("level", WARN.getLevel());
    WarnException warnException =
        new WarnException(
            (Integer) map.get("errCode"),
            "test",
            LinkisException.hostname,
            LinkisException.hostPort,
            LinkisException.applicationName);
    assertEquals(warnException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(warnException.toString(), ExceptionManager.generateException(map).toString());
    map.replace("level", FATAL.getLevel());
    FatalException fatalException =
        new FatalException(
            (Integer) map.get("errCode"),
            "test",
            LinkisException.hostname,
            LinkisException.hostPort,
            LinkisException.applicationName);
    assertEquals(fatalException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(fatalException.toString(), ExceptionManager.generateException(map).toString());
    map.replace("level", RETRY.getLevel());
    LinkisRetryException retryException =
        new LinkisRetryException(
            (Integer) map.get("errCode"),
            "test",
            LinkisException.hostname,
            LinkisException.hostPort,
            LinkisException.applicationName);
    assertEquals(retryException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(retryException.toString(), ExceptionManager.generateException(map).toString());
    map.replace("level", 123);
    map.put("test", 123);
    errorException.setErrCode(CommonErrorConstants.COMMON_ERROR());
    errorException.setDesc("Exception Map that cannot be parsed:(不能解析的异常Map：)" + map);
    assertEquals(errorException.getClass(), ExceptionManager.generateException(map).getClass());
    assertEquals(errorException.toString(), ExceptionManager.generateException(map).toString());
  }

  @Test
  void unknownException() {
    Map<String, Object> map = ExceptionManager.unknownException("test");
    Map<String, Object> assertMap = new HashMap<String, Object>();
    assertMap.put("level", ERROR.getLevel());
    assertMap.put("errCode", 0);
    assertMap.put("desc", "test");
    assertMap.put("ip", LinkisException.hostname);
    assertMap.put("port", LinkisException.hostPort);
    assertMap.put("serviceKind", LinkisException.applicationName);
    assertTrue(map.equals(assertMap));
  }
}
