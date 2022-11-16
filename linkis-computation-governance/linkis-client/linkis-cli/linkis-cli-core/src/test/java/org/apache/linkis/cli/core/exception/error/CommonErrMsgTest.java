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

package org.apache.linkis.cli.core.exception.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CommonErrMsgTest {

  @Test
  @DisplayName("enumTest")
  public void enumTest() {

    String templateGenerr = CommonErrMsg.TemplateGenErr.getMsgTemplate();
    String fitErrMsgTemplate = CommonErrMsg.TemplateFitErr.getMsgTemplate();
    String parserInitErrMsgTemplate = CommonErrMsg.ParserInitErr.getMsgTemplate();
    String parseErrMsgTemplate = CommonErrMsg.ParserParseErr.getMsgTemplate();

    Assertions.assertEquals("Cannot generate template. :{0}", templateGenerr);
    Assertions.assertEquals("Cannot fit input into template: {0}", fitErrMsgTemplate);

    Assertions.assertEquals("Failed to init parser: {0}", parserInitErrMsgTemplate);
    Assertions.assertEquals("Failed to parse. {0}", parseErrMsgTemplate);
  }
}
