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

package org.apache.linkis.cli.application.interactor.command.template;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.entity.command.CmdOption;
import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.CmdType;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.SpecialMap;
import org.apache.linkis.cli.application.interactor.command.template.converter.AbstractStringConverter;
import org.apache.linkis.cli.application.interactor.command.template.converter.PredefinedStringConverters;
import org.apache.linkis.cli.application.interactor.command.template.option.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * CmdTemplate defines what CmdOption/Flag/Parameter that a command should contains. StdOption:
 * CmdOption.name should starts with '-' String that follows is treated as CmdOption.value. User
 * should input both name and value e.g. --cmd "whoami". Flag: a special type of CmdOption. Only
 * boolean value allowed Parameter: User only input Parameter.value
 */
public abstract class AbstractCmdTemplate implements CmdTemplate, Cloneable {
  /** members */
  protected CmdType cmdType;

  protected List<CmdOption<?>> options;

  /** option name -> CmdOption/Flag/Parameter */
  protected Map<String, CmdOption<?>> optionsMap;

  public AbstractCmdTemplate(final CmdType cmdType) {
    this.cmdType = cmdType;
    options = new ArrayList<>();
    optionsMap = new HashMap<>();
  }

  /** For parameters */
  protected final Parameter<String> parameter(
      String keyPrefix,
      String key,
      String paramName,
      String description,
      boolean isOptional,
      String defaultValue) {
    return parameter(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        PredefinedStringConverters.NO_CONVERTER,
        defaultValue);
  }

  protected final Parameter<Integer> parameter(
      String keyPrefix,
      String key,
      String paramName,
      String description,
      boolean isOptional,
      Integer defaultValue) {
    return parameter(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        PredefinedStringConverters.INT_CONVERTER,
        defaultValue);
  }

  protected final Parameter<String[]> parameter(
      String keyPrefix,
      String key,
      String paramName,
      String description,
      boolean isOptional,
      String[] defaultValue) {
    return parameter(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        PredefinedStringConverters.STR_ARRAY_CONVERTER,
        defaultValue);
  }

  protected final <T> Parameter<T> parameter(
      String keyPrefix,
      String key,
      String paramName,
      String description,
      boolean isOptional,
      AbstractStringConverter<T> converter,
      T defaultValue) {
    Parameter<T> parameter =
        new Parameter<>(
            keyPrefix, key, paramName, description, isOptional, converter, defaultValue);
    options.add(parameter);
    return parameter;
  }

  /** For flags */
  protected final Flag flag(
      String keyPrefix, String key, String[] paramName, String description, boolean defaultValue) {
    return flag(keyPrefix, key, paramName, description, false, defaultValue);
  }

  protected final Flag flag(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      boolean defaultValue) {
    checkIllegalOption(paramName);
    Flag flag = new Flag(keyPrefix, key, paramName, description, isOptional, defaultValue);
    putOption(paramName, flag);
    return flag;
  }

  /** For options */
  protected final StdOption<String> option(
      String keyPrefix, String key, String[] paramName, String description, boolean isOptional) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        null,
        PredefinedStringConverters.NO_CONVERTER);
  }

  protected final StdOption<String> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      String defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.NO_CONVERTER);
  }

  protected final StdOption<String[]> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      String[] defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.STR_ARRAY_CONVERTER);
  }

  protected final StdOption<Integer> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      Integer defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.INT_CONVERTER);
  }

  protected final StdOption<Boolean> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      Boolean defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.BOOLEAN_CONVERTER);
  }

  protected final StdOption<Map<String, String>> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      Map<String, String> defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.STRING_MAP_CONVERTER);
  }

  protected final StdOption<SpecialMap<String, String>> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      SpecialMap<String, String> defaultValue) {
    return option(
        keyPrefix,
        key,
        paramName,
        description,
        isOptional,
        defaultValue,
        PredefinedStringConverters.STRING_SPECIAL_MAP_CONVERTER);
  }

  protected final <T> StdOption<T> option(
      String keyPrefix,
      String key,
      String[] paramName,
      String description,
      boolean isOptional,
      T defaultValue,
      AbstractStringConverter<T> converter) {
    checkIllegalOption(paramName);
    StdOption<T> stdOption =
        new StdOption<>(
            keyPrefix, key, paramName, description, isOptional, defaultValue, converter);
    putOption(paramName, stdOption);
    return stdOption;
  }

  protected final MapOption mapOption(
      String keyPrefix, String key, String[] paramName, String description, boolean isOptional) {
    checkIllegalOption(paramName);
    MapOption option = new MapOption(keyPrefix, key, paramName, description, isOptional);
    putOption(paramName, option);
    return option;
  }

  protected final SpecialMapOption speciaMapOption(
      String keyPrefix, String key, String[] paramName, String description, boolean isOptional) {
    checkIllegalOption(paramName);
    SpecialMapOption option =
        new SpecialMapOption(keyPrefix, key, paramName, description, isOptional);
    putOption(paramName, option);
    return option;
  }

  private void checkIllegalOption(final String[] names) {
    if (names == null || names.length <= 0) {
      throw new IllegalArgumentException("At least one cmdType should be given to CmdOption.");
    } else if (names.length > CliConstants.MAX_NUM_OF_COMMAND_ARGUEMENTS) {
      throw new IllegalArgumentException(
          "At most "
              + CliConstants.MAX_NUM_OF_COMMAND_ARGUEMENTS
              + " cmdType can be given to CmdOption.");
    } else {
      for (String name : names) {
        if (!name.startsWith("-")) {
          throw new IllegalArgumentException(name + " should starts with '-'.");
        }
      }
    }
  }

  private void putOption(final String[] names, CmdOption<?> option) {
    options.add(option);
    for (String name : names) {
      optionsMap.put(name, option);
    }
  }

  public CmdType getCmdType() {
    return this.cmdType;
  }

  @Override
  public List<CmdOption<?>> getOptions() {
    return this.options;
  }

  @Override
  public Map<String, CmdOption<?>> getOptionsMap() {
    return this.optionsMap;
  }

  public abstract void checkParams() throws LinkisClientRuntimeException;

  @Override
  protected Object clone() throws CloneNotSupportedException {
    AbstractCmdTemplate ret = (AbstractCmdTemplate) super.clone();

    /*
     for recording field -> paraName relation
    */
    Map<String, List<String>> tmpMap = new HashMap<>();
    for (Map.Entry<String, CmdOption<?>> entry : optionsMap.entrySet()) {
      String key = entry.getValue().getKey();
      if (!tmpMap.containsKey(key)) {
        tmpMap.put(key, new ArrayList<>());
      }
      tmpMap.get(key).add(entry.getKey());
    }

    ret.options = new ArrayList<>();
    ret.optionsMap = new HashMap<>();

    /*
     scan all filds (fields should contains all options/parameters/flags)
    */
    Class clazz = ret.getClass();
    List<Field> fields = new ArrayList<>();
    while (clazz != null) {
      fields.addAll(new ArrayList<>(Arrays.asList(clazz.getDeclaredFields())));
      clazz = clazz.getSuperclass();
    }
    for (Field field : fields) {
      Object fieldObj;
      field.setAccessible(true);
      try {
        fieldObj = field.get(ret);
      } catch (IllegalArgumentException | IllegalAccessException e) {
        throw new CommandException(
            "CMD0018",
            ErrorLevel.ERROR,
            CommonErrMsg.ParserParseErr,
            "failed to make deep copy of template: " + this.getCmdType(),
            e);
      }
      if (fieldObj instanceof StdOption<?>
          || fieldObj instanceof MapOption
          || fieldObj instanceof SpecialMapOption) {
        CmdOption<?> opt = ((CmdOption<?>) fieldObj).clone();
        try {
          field.set(ret, opt);
        } catch (Exception e) {
          throw new CommandException(
              "CMD0018",
              ErrorLevel.ERROR,
              CommonErrMsg.ParserParseErr,
              "failed to make deep copy of template: " + this.getCmdType(),
              e);
        }
        ret.options.add(opt);
        List<String> paraNames = tmpMap.get(opt.getKey());
        /*
         reconstruct optionsMap
        */
        for (String paraName : paraNames) {
          ret.optionsMap.put(paraName, opt);
        }
      } else if (fieldObj instanceof Parameter<?>) {
        Parameter<?> param = ((Parameter<?>) fieldObj).clone();
        try {
          field.set(ret, param);
        } catch (Exception e) {
          throw new CommandException(
              "CMD0018",
              ErrorLevel.ERROR,
              CommonErrMsg.ParserParseErr,
              "failed to make deep copy of template: " + this.getCmdType(),
              e);
        }
        ret.options.add(param);
      } else {
        // ignore
      }
    }
    return ret;
  }

  @Override
  public AbstractCmdTemplate getCopy() {
    AbstractCmdTemplate ret;
    try {
      ret = (AbstractCmdTemplate) this.clone();
    } catch (CloneNotSupportedException e) {
      throw new CommandException(
          "CMD0018",
          ErrorLevel.ERROR,
          CommonErrMsg.ParserParseErr,
          "failed to make deep copy of template: " + this.getCmdType(),
          e);
    }

    return ret;
  }
}
