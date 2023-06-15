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

package org.apache.linkis.cli.application.operator.ujes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UJESConstantsTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    int exceptionCheckAfterPage = UJESConstants.EXCEPTION_CHECK_AFTER_PAGE;

    Long driverQuerySleepMills = UJESConstants.DRIVER_QUERY_SLEEP_MILLS;
    Integer driverRequestMaxRetryTime = UJESConstants.DRIVER_REQUEST_MAX_RETRY_TIME;
    Integer maxLogSize = UJESConstants.MAX_LOG_SIZE;
    Integer idxForLogTypeAll = UJESConstants.IDX_FOR_LOG_TYPE_ALL;

    String queryPageSizeName = UJESConstants.QUERY_PAGE_SIZE_NAME;
    int queryPageSizeDefaultValue = UJESConstants.QUERY_PAGE_SIZE_DEFAULT_VALUE;
    String queryStatusName = UJESConstants.QUERY_STATUS_NAME;
    String queryStartDateMillsName = UJESConstants.QUERY_START_DATE_MILLS_NAME;
    String queryEndDateMillsName = UJESConstants.QUERY_END_DATE_MILLS_NAME;
    String queryPageNowName = UJESConstants.QUERY_PAGE_NOW_NAME;

    Assertions.assertTrue(10000 == exceptionCheckAfterPage);
    Assertions.assertTrue(500L == driverQuerySleepMills);
    Assertions.assertTrue(3 == driverRequestMaxRetryTime.intValue());
    Assertions.assertTrue(-1 == maxLogSize);
    Assertions.assertTrue(3 == idxForLogTypeAll.intValue());
    Assertions.assertEquals("pageSize", queryPageSizeName);
    Assertions.assertTrue(100 == queryPageSizeDefaultValue);
    Assertions.assertEquals("status", queryStatusName);
    Assertions.assertEquals("startDateMills", queryStartDateMillsName);
    Assertions.assertEquals("endDateMills", queryEndDateMillsName);
    Assertions.assertEquals("pageNow", queryPageNowName);
  }
}
