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

package org.apache.linkis.filesystem.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WorkSpaceExceptionTest {

  @Test
  @DisplayName("workSpaceExceptionTest")
  public void workSpaceExceptionTest() {

    int errorCode = 80001;
    String errorMsg =
        "Requesting IO-Engine to initialize fileSystem failed!(请求IO-Engine初始化fileSystem失败！)";
    String ip = "127.0.0.1";
    int port = 8081;
    String serviceKind = "ps-service";
    WorkSpaceException workSpaceException = new WorkSpaceException(errorCode, errorMsg);

    Assertions.assertTrue(errorCode == workSpaceException.getErrCode());
    Assertions.assertEquals(errorMsg, workSpaceException.getDesc());

    WorkSpaceException spaceException =
        new WorkSpaceException(errorCode, errorMsg, ip, port, serviceKind);
    Assertions.assertEquals(ip, spaceException.getIp());
    Assertions.assertTrue(port == spaceException.getPort());
    Assertions.assertEquals(serviceKind, spaceException.getServiceKind());
  }
}
