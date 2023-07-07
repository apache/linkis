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

import org.apache.linkis.cli.application.entity.command.CmdOption;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.ParamItem;
import org.apache.linkis.cli.application.entity.command.Params;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.TransformerException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.SpecialMap;
import org.apache.linkis.cli.application.interactor.command.fitter.Fitter;
import org.apache.linkis.cli.application.interactor.command.parser.result.ParseResult;
import org.apache.linkis.cli.application.interactor.command.parser.transformer.ParamKeyMapper;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstarctParser implements Parser {
  private static final Logger logger = LoggerFactory.getLogger(AbstarctParser.class);

  Fitter fitter;
  CmdTemplate template;
  ParamKeyMapper mapper;

  public AbstarctParser setFitter(Fitter fitter) {
    this.fitter = fitter;
    return this;
  }

  public AbstarctParser setTemplate(CmdTemplate template) {
    this.template = template;
    return this;
  }

  public AbstarctParser setMapper(ParamKeyMapper mapper) {
    this.mapper = mapper;
    return this;
  }

  public void checkInit() {
    if (fitter == null) {
      throw new CommandException(
          "CMD0013",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserInitErr,
          "failed to init parser: \n" + "fitter is null");
    }
    if (template == null) {
      throw new CommandException(
          "CMD0013",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserInitErr,
          "failed to init parser: \n" + "template is null");
    }
  }

  public Params templateToParams(CmdTemplate template, ParamKeyMapper mapper) {
    List<CmdOption<?>> options = template.getOptions();

    Map<String, ParamItem> params = new HashMap<>();
    StringBuilder mapperInfoSb = new StringBuilder();

    for (CmdOption<?> option : options) {
      ParamItem paramItem = optionToParamItem(option, params, mapper, mapperInfoSb);
      if (params.containsKey(paramItem.getKey())) {
        throw new TransformerException(
            "TFM0012",
            ErrorLevel.ERROR,
            CommonErrMsg.TransformerException,
            MessageFormat.format(
                "Failed to convert option into ParamItem: params contains duplicated identifier: \"{0}\"",
                option.getKey()));

      } else {
        params.put(paramItem.getKey(), paramItem);
      }
    }

    if (mapper != null) {
      logger.info("\nParam Key Substitution: " + mapperInfoSb.toString());
    }
    Map<String, Object> extraProperties = new HashMap<>();
    return new Params(null, template.getCmdType(), params, extraProperties);
  }

  protected ParamItem optionToParamItem(
      CmdOption<?> option,
      Map<String, ParamItem> params,
      ParamKeyMapper mapper,
      StringBuilder mapperInfoSb) {
    String oriKey = option.getKey();
    String keyPrefix = option.getKeyPrefix();
    String key = oriKey;
    if (params.containsKey(oriKey)) {
      throw new TransformerException(
          "TFM0012",
          ErrorLevel.ERROR,
          CommonErrMsg.TransformerException,
          MessageFormat.format(
              "Failed to convert option into ParamItem: params contains duplicated identifier: \"{0}\"",
              option.getKey()));
    }
    if (mapper != null) {
      key = getMappedKey(oriKey, mapper, mapperInfoSb);
    }
    Object val = option.getValue();
    if (option.getValue() != null
        && option.getValue() instanceof Map
        && !(option.getValue() instanceof SpecialMap)) {
      Map<String, Object> subMap;
      try {
        subMap = (Map<String, Object>) option.getValue();
      } catch (Exception e) {
        logger.warn("Failed to get subMap for option: " + option.getKey() + ".", e);
        return null;
      }
      if (mapper != null) {
        subMap = mapper.getMappedMapping(subMap);
      }
      val = addPrefixToSubMapKey(subMap, keyPrefix);
    }
    return new ParamItem(keyPrefix, key, val, option.hasVal(), option.getDefaultValue());
  }

  private Map<String, Object> addPrefixToSubMapKey(Map<String, Object> subMap, String keyPrefix) {
    Map<String, Object> newSubMap = new HashMap<>();
    StringBuilder keyBuilder = new StringBuilder();
    for (Map.Entry<String, Object> entry : subMap.entrySet()) {
      if (StringUtils.isNotBlank(keyPrefix) && !StringUtils.startsWith(entry.getKey(), keyPrefix)) {
        keyBuilder.append(keyPrefix).append('.').append(entry.getKey());
      } else {
        keyBuilder.append(entry.getKey());
      }
      newSubMap.put(keyBuilder.toString(), entry.getValue());
      keyBuilder.setLength(0);
    }
    return newSubMap;
  }

  protected String getMappedKey(String keyOri, ParamKeyMapper mapper, StringBuilder mapperInfoSb) {
    /** Transform option keys */
    String key = mapper.getMappedKey(keyOri);
    if (!key.equals(keyOri)) {
      mapperInfoSb.append("\n\t").append(keyOri).append(" ==> ").append(key);
    }
    return key;
  }

  @Override
  public abstract ParseResult parse(String[] input);
}
