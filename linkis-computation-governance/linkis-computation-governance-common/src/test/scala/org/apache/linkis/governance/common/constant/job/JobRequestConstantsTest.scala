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

package org.apache.linkis.governance.common.constant.job

import org.junit.jupiter.api.{Assertions, DisplayName, Test}

class JobRequestConstantsTest {

  @Test
  @DisplayName("constTest")
  def constTest(): Unit = {

    val jobid = JobRequestConstants.JOB_ID
    val jobhistorylist = JobRequestConstants.JOB_HISTORY_LIST
    val jobrequestlist = JobRequestConstants.JOB_REQUEST_LIST
    val jobdetaillist = JobRequestConstants.JOB_DETAIL_LIST
    val totalpage = JobRequestConstants.TOTAL_PAGE

    Assertions.assertEquals("jobId", jobid)
    Assertions.assertEquals("jobHistoryList", jobhistorylist)
    Assertions.assertEquals("jobRequestList", jobrequestlist)
    Assertions.assertEquals("jobDetailList", jobdetaillist)
    Assertions.assertEquals("totalPage", totalpage)
  }

}
