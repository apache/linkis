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

public class LinkisKeysTest {

  @Test
  @DisplayName("constTest")
  public void constTest() {

    String keyCodetype = LinkisKeys.KEY_CODETYPE;
    String keyCode = LinkisKeys.KEY_CODE;
    String keyVars = LinkisKeys.KEY_VARS;
    String keyConf = LinkisKeys.KEY_CONF;
    String keyScriptPath = LinkisKeys.KEY_SCRIPT_PATH;
    String keyEnginetype = LinkisKeys.KEY_ENGINETYPE;
    String keyUserCreator = LinkisKeys.KEY_USER_CREATOR;
    String keyErrorCode = LinkisKeys.KEY_ERROR_CODE;
    String keyErrorDesc = LinkisKeys.KEY_ERROR_DESC;
    String keyStrongerExecid = LinkisKeys.KEY_STRONGER_EXECID;
    String keyRequestapp = LinkisKeys.KEY_REQUESTAPP;
    String keyExecid = LinkisKeys.KEY_EXECID;
    String keyUmuser = LinkisKeys.KEY_UMUSER;
    String keyExecuteonce = LinkisKeys.KEY_EXECUTEONCE;

    Assertions.assertEquals("codeType", keyCodetype);
    Assertions.assertEquals("code", keyCode);
    Assertions.assertEquals("variables", keyVars);
    Assertions.assertEquals("configuration", keyConf);
    Assertions.assertEquals("scriptPath", keyScriptPath);
    Assertions.assertEquals("engineType", keyEnginetype);
    Assertions.assertEquals("userCreator", keyUserCreator);
    Assertions.assertEquals("errCode", keyErrorCode);
    Assertions.assertEquals("errDesc", keyErrorDesc);
    Assertions.assertEquals("strongerExecId", keyStrongerExecid);

    Assertions.assertEquals("requestApplicationName", keyRequestapp);
    Assertions.assertEquals("execId", keyExecid);
    Assertions.assertEquals("umUser", keyUmuser);
    Assertions.assertEquals("executeOnce", keyExecuteonce);
  }
}
