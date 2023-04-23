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

package org.apache.linkis.cli.application.interactor.validate;

import org.apache.linkis.cli.application.entity.command.CmdOption;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.ValidateException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import java.text.MessageFormat;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1. Check if there is missing or unknown option. 2. Call checkParam method for command-specific
 * validation.
 */
public class ParsedTplValidator {
  private static final Logger logger = LoggerFactory.getLogger(ParsedTplValidator.class);

  public void doValidation(CmdTemplate parsedTemplateCopy) throws CommandException {

    String msg = "start validating command \"{0}\", template \"{1}\"";
    logger.info(
        MessageFormat.format(
            msg, parsedTemplateCopy.getCmdType().getName(), parsedTemplateCopy.getCmdType()));

    checkOptions(parsedTemplateCopy);

    logger.info("Start params-check");
    parsedTemplateCopy.checkParams();
    logger.info("params-check ok.");
  }

  /** Validation */
  private void checkOptions(CmdTemplate template) throws CommandException {
    List<CmdOption<?>> options = template.getOptions();
    for (CmdOption<?> cmdOption : options) {
      if (!cmdOption.hasVal() && !cmdOption.isOptional()) {
        throw new ValidateException(
            "VLD0003",
            ErrorLevel.ERROR,
            CommonErrMsg.ValidationErr,
            "CmdOption value cannot be empty: paramName:"
                + cmdOption.getParamName()
                + "CmdType: "
                + template.getCmdType());
      }
    }
  }
}
