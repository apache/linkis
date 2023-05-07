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

package org.apache.linkis.cli.application.interactor.command.fitter;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.entity.command.CmdOption;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.CmdType;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.template.option.Flag;
import org.apache.linkis.cli.application.interactor.command.template.option.Parameter;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFitter implements Fitter {

  private static final Logger logger = LoggerFactory.getLogger(AbstractFitter.class);

  /**
   * Parse arguments based on template. Any redundant argument will be stored for further parsing.
   *
   * @throws LinkisClientRuntimeException
   */
  @Override
  public abstract FitterResult fit(String[] inputs, CmdTemplate templateCopy)
      throws LinkisClientRuntimeException;

  protected CmdTemplate doFit(String[] args, CmdTemplate templateCopy, List<String> remains)
      throws LinkisClientRuntimeException {
    String msg = "Parsing command: \"{0}\" into template: \"{1}\"";
    logger.info(MessageFormat.format(msg, StringUtils.join(args, " "), templateCopy.getCmdType()));
    List<CmdOption<?>> parameters = new ArrayList<>();
    for (CmdOption<?> option : templateCopy.getOptions()) {
      if (option instanceof Parameter<?>) {
        parameters.add(option);
      }
    }
    doFit(
        args,
        0,
        templateCopy.getCmdType(),
        templateCopy.getOptionsMap(),
        templateCopy.getOptions(),
        parameters,
        remains);

    return templateCopy;
  }

  /**
   * Parse arguments one by one. If an Option/Flag is not defined in 'commandTemplate', then record
   * it in 'remains' If all parameters defined in 'commandTemplate' are already set, then any other
   * Parameter is recorded in 'remains'
   *
   * @throws LinkisClientRuntimeException when some argument is not configured
   */
  private final void doFit(
      final String[] args,
      int start,
      CmdType cmdType,
      Map<String, CmdOption<?>> optionsMap,
      List<CmdOption<?>> options,
      List<CmdOption<?>> parameters,
      List<String> remains)
      throws LinkisClientRuntimeException {
    doFit(args, start, 0, cmdType, optionsMap, options, parameters, remains);
  }

  private final void doFit(
      final String[] args,
      final int argIdx,
      final int paraIdx,
      CmdType cmdType,
      Map<String, CmdOption<?>> optionsMap,
      List<CmdOption<?>> options,
      List<CmdOption<?>> parameters,
      List<String> remains)
      throws LinkisClientRuntimeException {
    if (args.length <= argIdx || args.length <= paraIdx) {
      return;
    }

    if (FitterUtils.isOption(args[argIdx]) && optionsMap.containsKey(args[argIdx])) {

      int index = setOptionValue(args, argIdx, cmdType, optionsMap, remains);
      // fit from the new index
      doFit(args, index, paraIdx, cmdType, optionsMap, options, parameters, remains);
    } else {
      int index = setParameterValue(args, argIdx, paraIdx, parameters, remains);
      // fit from argIdx + 1
      doFit(args, index, paraIdx + 1, cmdType, optionsMap, options, parameters, remains);
    }
  }

  /**
   * If an input option is not defined by template. Then its name and value(if exists) is recorded
   * in 'remains'
   *
   * @param args java arguments
   * @param index argument index
   * @param cmdType command type
   * @param optionsMap optionName -> option
   * @param remains what's left after parsing
   * @return next argument index
   * @throws LinkisClientRuntimeException when some argument is not configured
   */
  private final int setOptionValue(
      final String[] args,
      final int index,
      CmdType cmdType,
      Map<String, CmdOption<?>> optionsMap,
      List<String> remains)
      throws LinkisClientRuntimeException {
    int next = index + 1;
    String arg = args[index];
    if (optionsMap.containsKey(args[index])) {
      CmdOption<?> cmdOption = optionsMap.get(arg);
      if (cmdOption instanceof Flag) {
        try {
          cmdOption.setValueWithStr("true");
        } catch (IllegalArgumentException ie) {
          String msg =
              MessageFormat.format(
                  "Illegal Arguement \"{0}\" for option \"{1}\"",
                  args[next], cmdOption.getParamName());
          throw new CommandException(
              "CMD0010", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, cmdType, msg);
        }
        return next;
      } else if (cmdOption instanceof CmdOption<?>) {
        if (next >= args.length || FitterUtils.isOption(args[next])) {
          String msg =
              MessageFormat.format(
                  "Cannot parse command: option \"{0}\" is specified without value.", arg);
          throw new CommandException(
              "CMD0011", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, cmdType, msg);
        }
        try {
          cmdOption.setValueWithStr(args[next]);
        } catch (IllegalArgumentException ie) {
          String msg =
              MessageFormat.format(
                  "Illegal Arguement \"{0}\" for option \"{1}\". Msg: {2}",
                  args[next], cmdOption.getParamName(), ie.getMessage());
          throw new CommandException(
              "CMD0010", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, cmdType, msg);
        }

        return next + 1;
      } else {
        throw new CommandException(
            "CMD0010",
            ErrorLevel.ERROR,
            CommonErrMsg.TemplateFitErr,
            "Failed to set option value: optionMap contains objects that is not Option!");
      }
    } else {
      remains.add(arg);
      if (next < args.length && !FitterUtils.isOption(args[next])) {
        remains.add(args[next]);
        return next + 1;
      } else {
        return next;
      }
    }
  }

  /**
   * If number of user input parameter is larger than what's defined in template, then set parameter
   * value based on input order and record the rests in 'remains'.
   *
   * @param args java options
   * @param argIdx argument index
   * @param paraIdx index of Parameter
   * @return next argument index
   * @throws LinkisClientRuntimeException
   */
  private final int setParameterValue(
      final String[] args,
      final int argIdx,
      final int paraIdx,
      List<CmdOption<?>> parameters,
      List<String> remains)
      throws LinkisClientRuntimeException {

    if (parameters.size() <= paraIdx) {
      remains.add(args[argIdx]);
      return argIdx + 1;
    }
    CmdOption<?> cmdOption = parameters.get(paraIdx);
    if (!(cmdOption instanceof Parameter<?>)) {
      throw new CommandException(
          "CMD001",
          ErrorLevel.ERROR,
          CommonErrMsg.TemplateFitErr,
          "Failed to set param value: parameters contains objects that is not Parameter!");
    }
    Parameter<?> param = (Parameter<?>) cmdOption;
    if (param.accepctArrayValue()) {
      String[] args2 = Arrays.copyOfRange(args, argIdx, args.length);
      param.setValueWithStr(StringUtils.join(args2, CliConstants.ARRAY_SEQ));
      return args.length;
    } else {
      parameters.get(paraIdx).setValueWithStr(args[argIdx]);
      return argIdx + 1;
    }
  }
}
