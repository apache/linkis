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

package org.apache.linkis.jobhistory.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** JobHistory Tester */
public class JobHistoryTest {

  @Test
  @DisplayName("Method description: ...")
  public void testGetUpdateTimeMills() throws Exception {
    long time = 1643738522000L; // 2022-02-02 02:02:02.000
    // String timestr = "2022-02-02 02:02:02.000";

    JobHistory jobHistory = new JobHistory();
    Date date = new Date(time);
    jobHistory.setCreatedTime(date);
    jobHistory.setUpdatedTime(date);

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    String expectedStr = dateFormat.format(date);
    assertEquals(expectedStr, jobHistory.getUpdateTimeMills());
  }
}
