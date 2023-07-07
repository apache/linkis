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

package org.apache.linkis.cli.application.interactor.command.parser;

import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.Params;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.fitter.FitterResult;
import org.apache.linkis.cli.application.interactor.command.parser.result.ParseResult;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleCmdParser extends AbstarctParser {
  private static final Logger logger = LoggerFactory.getLogger(SingleCmdParser.class);

  @Override
  public ParseResult parse(String[] input) {
    checkInit();

    if (input == null || input.length == 0) {
      throw new CommandException(
          "CMD0015",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserParseErr,
          template.getCmdType(),
          "nothing to parse");
    }

    FitterResult result = fitter.fit(input, template);

    String[] remains = result.getRemains();

    if (remains != null && remains.length != 0) {
      throw new CommandException(
          "CMD0022",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserParseErr,
          template.getCmdType(),
          "Cannot parse argument(s): " + Arrays.toString(remains) + ". Please check help message");
    }

    CmdTemplate parsedTemplate = result.getParsedTemplate();
    Params param = templateToParams(parsedTemplate, mapper);

    return new ParseResult(parsedTemplate, param, remains);
  }
}
