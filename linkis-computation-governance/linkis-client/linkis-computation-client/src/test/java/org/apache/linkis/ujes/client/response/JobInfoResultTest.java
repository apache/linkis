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

package org.apache.linkis.ujes.client.response;

import org.apache.linkis.governance.common.entity.task.RequestPersistTask;
import org.apache.linkis.ujes.client.UJESClient;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class JobInfoResultTest {

  /** verify single path returns check point 1: return one path */
  @Test
  void shouldReturnResultSetWithOrder() {
    String[] toBeReturned = new String[] {"hdfs://hdfs/path/test/mockFile_1.dolphi"};
    String[] setList = getResultSetList(toBeReturned);
    assertEquals(1, setList.length);
    assertEquals("hdfs://hdfs/path/test/mockFile_1.dolphi", setList[0]);
  }

  /** verify empty path set check point 1: return empty path */
  @Test
  void shouldReturnEmptyResultSet() {
    String[] toBeReturned = new String[] {};
    String[] setList = getResultSetList(toBeReturned);
    assertEquals(0, setList.length);
  }

  /**
   * verify multiple result set, sorted by file name with numbers check point 1: sort asc check
   * point 2: sort by number, not ascii
   */
  @Test
  void shouldReturnMultiResultSetWithOrder() {
    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
    if (!isWindows) {
      String[] toBeReturned =
          new String[] {
            "/path/to/xxxx_1.txt",
            "/some/path/xxxx_10.txt",
            "/another/path/xxxx_0.txt",
            "/another/path/xxxx_2.txt",
            "/yet/another/path/xxxx_3.txt",
          };
      String[] setList = getResultSetList(toBeReturned);
      assertIterableEquals(
          Lists.newArrayList(
              "/another/path/xxxx_0.txt",
              "/path/to/xxxx_1.txt",
              "/another/path/xxxx_2.txt",
              "/yet/another/path/xxxx_3.txt",
              "/some/path/xxxx_10.txt"),
          Lists.newArrayList(setList));
    }
  }

  private static String[] getResultSetList(String[] toBeReturned) {
    JobInfoResult jobInfoResult = Mockito.spy(new JobInfoResult());

    UJESClient ujesClient = Mockito.mock(UJESClient.class);
    Mockito.doReturn("Succeed").when(jobInfoResult).getJobStatus();
    RequestPersistTask persistTask = new RequestPersistTask();
    persistTask.setUmUser("test");
    persistTask.setResultLocation("mockPath");
    Mockito.doReturn(persistTask).when(jobInfoResult).getRequestPersistTask();

    ResultSetListResult t = Mockito.spy(new ResultSetListResult());
    Mockito.when(ujesClient.executeUJESJob(any())).thenReturn(t);
    Mockito.doReturn(toBeReturned).when(t).getResultSetList();

    return jobInfoResult.getResultSetList(ujesClient);
  }
}
