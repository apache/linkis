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

package org.apache.linkis.cli.application.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AppConstantsTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String defaultConfigName = AppConstants.DEFAULT_CONFIG_NAME;
    Long jobQuerySleepMills = AppConstants.JOB_QUERY_SLEEP_MILLS;
    String resultsetLogo = AppConstants.RESULTSET_LOGO;
    String resultsetMetaBeginLogo = AppConstants.RESULTSET_META_BEGIN_LOGO;
    String resultsetMetaEndLogo = AppConstants.RESULTSET_META_END_LOGO;
    String resultsetSeparatorLogo = AppConstants.RESULTSET_SEPARATOR_LOGO;
    Integer resultsetPageSize = AppConstants.RESULTSET_PAGE_SIZE;
    String jobCreatorDefault = AppConstants.JOB_CREATOR_DEFAULT;
    String jobCreatorAsyncDefault = AppConstants.JOB_CREATOR_ASYNC_DEFAULT;
    String dummyCid = AppConstants.DUMMY_CID;
    String linkisCli = AppConstants.LINKIS_CLI;
    String ujesMode = AppConstants.UJES_MODE;
    String onceMode = AppConstants.ONCE_MODE;

    Assertions.assertEquals("linkis-cli.properties", defaultConfigName);
    Assertions.assertTrue(2000L == jobQuerySleepMills.longValue());
    Assertions.assertEquals("============ RESULT SET {0} ============", resultsetLogo);
    Assertions.assertEquals("----------- META DATA ------------", resultsetMetaBeginLogo);
    Assertions.assertEquals("------------ END OF META DATA ------------", resultsetMetaEndLogo);
    Assertions.assertEquals("------------------------", resultsetSeparatorLogo);
    Assertions.assertTrue(5000 == resultsetPageSize.intValue());
    Assertions.assertEquals("LINKISCLI", jobCreatorDefault);
    Assertions.assertEquals("LINKISCLIASYNC", jobCreatorAsyncDefault);
    Assertions.assertEquals("dummy", dummyCid);
    Assertions.assertEquals("LinkisCli", linkisCli);
    Assertions.assertEquals("ujes", ujesMode);
    Assertions.assertEquals("once", onceMode);
  }
}
