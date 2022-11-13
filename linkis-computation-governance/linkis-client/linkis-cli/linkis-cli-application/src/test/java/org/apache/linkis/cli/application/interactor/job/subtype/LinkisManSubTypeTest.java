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

package org.apache.linkis.cli.application.interactor.job.subtype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LinkisManSubTypeTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String killName = LinkisManSubType.KILL.getName();
    String logName = LinkisManSubType.LOG.getName();
    String descName = LinkisManSubType.DESC.getName();
    String statusName = LinkisManSubType.STATUS.getName();
    String listName = LinkisManSubType.LIST.getName();
    String resultName = LinkisManSubType.RESULT.getName();

    Assertions.assertEquals("kill", killName);
    Assertions.assertEquals("log", logName);
    Assertions.assertEquals("desc", descName);
    Assertions.assertEquals("status", statusName);
    Assertions.assertEquals("list", listName);
    Assertions.assertEquals("result", resultName);
  }
}
