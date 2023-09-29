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

package org.apache.linkis.cli.core.constants;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommonConstantsTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    Long jobQuerySleepMills = CommonConstants.JOB_QUERY_SLEEP_MILLS;
    Integer requestMaxRetryTime = CommonConstants.REQUEST_MAX_RETRY_TIME;
    String universalSubcmd = CommonConstants.UNIVERSAL_SUBCMD;
    String universalSubcmdDesc = CommonConstants.UNIVERSAL_SUBCMD_DESC;
    String successIndicator = CommonConstants.SUCCESS_INDICATOR;
    String failureIndicator = CommonConstants.FAILURE_INDICATOR;
    String arraySeq = CommonConstants.ARRAY_SEQ;
    String arraySeqRegex = CommonConstants.ARRAY_SEQ_REGEX;
    int maxNumOfCommandArguements = CommonConstants.MAX_NUM_OF_COMMAND_ARGUEMENTS;
    String configDir = CommonConstants.CONFIG_DIR;
    String[] configExtension = CommonConstants.CONFIG_EXTENSION;
    String systemPropertiesIdentifier = CommonConstants.SYSTEM_PROPERTIES_IDENTIFIER;
    String systemEnvIdentifier = CommonConstants.SYSTEM_ENV_IDENTIFIER;

    Assertions.assertTrue(2000l == jobQuerySleepMills);
    Assertions.assertTrue(3 == requestMaxRetryTime);
    Assertions.assertEquals("linkis-cli", universalSubcmd);
    Assertions.assertEquals(
        "command for all types of jobs supported by Linkis", universalSubcmdDesc);

    Assertions.assertEquals("############Execute Success!!!########", successIndicator);
    Assertions.assertEquals("############Execute Error!!!########", failureIndicator);
    Assertions.assertEquals("@#@", arraySeq);
    Assertions.assertEquals("(?=([^\"]*\"[^\"]*\")*[^\"]*$)", arraySeqRegex);
    Assertions.assertTrue(10 == maxNumOfCommandArguements);
    Assertions.assertEquals("config.path", configDir);
    Assertions.assertTrue(configExtension.length == 1);
    Assertions.assertEquals("SYS_PROP", systemPropertiesIdentifier);
    Assertions.assertEquals("SYS_ENV", systemEnvIdentifier);
  }
}
