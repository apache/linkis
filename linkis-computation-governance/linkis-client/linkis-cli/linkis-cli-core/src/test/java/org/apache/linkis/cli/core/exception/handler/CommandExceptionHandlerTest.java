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

package org.apache.linkis.cli.core.exception.handler;

import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.common.exception.handler.ExceptionHandler;
import org.apache.linkis.cli.core.exception.CommandException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.command.TestCmdType;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CommandExceptionHandlerTest {
  ExceptionHandler handler = new CommandExceptionHandler();

  // todo
  @Disabled
  @Test
  public void handle() throws Exception {
    CommandException cmdException =
        new CommandException(
            "CODE-001",
            ErrorLevel.ERROR,
            CommonErrMsg.TemplateGenErr,
            "Failed to generate template.");
    assertDoesNotThrow(() -> handler.handle(cmdException));

    String[] params = {"param1", "param2"};
    CommandException cmdException2 =
        new CommandException(
            "CODE-001",
            ErrorLevel.ERROR,
            CommonErrMsg.TemplateGenErr,
            params,
            "Failed to generate template.");
    assertDoesNotThrow(() -> handler.handle(cmdException2));

    CommandException cmdException3 =
        new CommandException(
            "CODE-001",
            ErrorLevel.ERROR,
            CommonErrMsg.TemplateGenErr,
            TestCmdType.PRIMARY,
            "Failed to generate template.");
    assertDoesNotThrow(() -> handler.handle(cmdException3));

    CommandException cmdException4 =
        new CommandException(
            "CODE-001",
            ErrorLevel.ERROR,
            CommonErrMsg.TemplateGenErr,
            TestCmdType.PRIMARY,
            params,
            "Failed to generate template.");
    assertDoesNotThrow(() -> handler.handle(cmdException4));
  }
}
