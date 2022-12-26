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

package org.apache.linkis.common.errorcode;

import org.junit.jupiter.api.Test;

import static org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.CREATE_HIVE_EXECUTOR_ERROR;
import static org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.GET_FIELD_SCHEMAS_ERROR;
import static org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.HIVE_EXEC_JAR_ERROR;
import static org.apache.linkis.engineplugin.hive.errorcode.HiveErrorCodeSummary.INVALID_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HiveErrorCodeSummaryTest {
  @Test
  void testGetErrorCode() {
    assertEquals(26040, CREATE_HIVE_EXECUTOR_ERROR.getErrorCode());
    assertEquals(26041, HIVE_EXEC_JAR_ERROR.getErrorCode());
    assertEquals(26042, GET_FIELD_SCHEMAS_ERROR.getErrorCode());
    assertEquals(26043, INVALID_VALUE.getErrorCode());
  }

  @Test
  void testGetErrorDesc() {
    assertEquals(
        "failed to create hive executor(创建hive执行器失败)", CREATE_HIVE_EXECUTOR_ERROR.getErrorDesc());
    assertEquals(
        "cannot find hive-exec.jar, start session failed(找不到 hive-exec.jar，启动会话失败)",
        HIVE_EXEC_JAR_ERROR.getErrorDesc());
    assertEquals(
        "cannot get the field schemas(无法获取字段 schemas)", GET_FIELD_SCHEMAS_ERROR.getErrorDesc());
    assertEquals("invalid value(无效值)", INVALID_VALUE.getErrorDesc());
  }
}
