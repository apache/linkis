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

package org.apache.linkis.cli.application.interactor.command.template.converter;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.command.SpecialMap;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PredefinedStringConverters {

  public static final AbstractStringConverter<String> NO_CONVERTER =
      new AbstractStringConverter<String>() {
        @Override
        public String convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }
          return from;
        }
      };

  public static final AbstractStringConverter<String[]> STR_ARRAY_CONVERTER =
      new AbstractStringConverter<String[]>() {
        @Override
        public String[] convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }
          String[] ret = from.trim().split(CliConstants.ARRAY_SEQ);
          for (int i = 0; i < ret.length; i++) {
            ret[i] = StringUtils.strip(ret[i], " \"");
          }
          return ret;
        }
      };

  public static final AbstractStringConverter<Map<String, String>> STRING_MAP_CONVERTER =
      new AbstractStringConverter<Map<String, String>>() {
        @Override
        public Map<String, String> convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }
          Map<String, String> paraMap = new HashMap<>();
          String[] arr = from.trim().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
          for (String prop : arr) {
            prop = prop.trim();
            int index = prop.indexOf("=");
            if (index != -1) {
              paraMap.put(
                  StringUtils.strip(prop.substring(0, index).trim(), " \""),
                  StringUtils.strip(prop.substring(index + 1).trim(), " \""));
            } else {
              throw new CommandException(
                  "CMD0021",
                  ErrorLevel.ERROR,
                  CommonErrMsg.ParserParseErr,
                  "Illegal Input: "
                      + from
                      + ". Input should be a Map described by kv-pairs. e.g. key1=value1,key2=value2");
            }
          }
          return paraMap;
        }
      };

  public static final AbstractStringConverter<SpecialMap<String, String>>
      STRING_SPECIAL_MAP_CONVERTER =
          new AbstractStringConverter<SpecialMap<String, String>>() {
            @Override
            public SpecialMap<String, String> convert(String from) {
              if (StringUtils.isBlank(from)) {
                return null;
              }
              SpecialMap<String, String> paraMap = new SpecialMap<>();
              String[] arr = from.trim().split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
              for (String prop : arr) {
                prop = prop.trim();
                int index = prop.indexOf("=");
                if (index != -1) {
                  paraMap.put(
                      StringUtils.strip(prop.substring(0, index).trim(), " \""),
                      StringUtils.strip(prop.substring(index + 1).trim(), " \""));
                } else {
                  throw new CommandException(
                      "CMD0021",
                      ErrorLevel.ERROR,
                      CommonErrMsg.ParserParseErr,
                      "Illegal Input: "
                          + from
                          + ". Input should be a Map described by kv-pairs. e.g. key1=value1,key2=value2");
                }
              }
              return paraMap;
            }
          };

  public static final AbstractStringConverter<Boolean> BOOLEAN_CONVERTER =
      new AbstractStringConverter<Boolean>() {
        @Override
        public Boolean convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }
          return Boolean.valueOf(from);
        }
      };

  public static final AbstractStringConverter<Integer> INT_CONVERTER =
      new AbstractStringConverter<Integer>() {
        @Override
        public Integer convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }
          return Integer.valueOf(from);
        }
      };

  public static final AbstractStringConverter<Long> LONG_CONVERTER =
      new AbstractStringConverter<Long>() {
        @Override
        public Long convert(String from) {
          return Long.valueOf(from);
        }
      };

  public static final AbstractStringConverter<List<Long>> LONG_ARRAY_CONVERTER =
      new AbstractStringConverter<List<Long>>() {
        @Override
        public List<Long> convert(String from) {
          if (StringUtils.isBlank(from)) {
            return null;
          }

          String[] split = from.split("\\s*,\\s*");
          if (split.length <= 0) {
            return new ArrayList<>(0);
          }

          List<Long> ret = new ArrayList<>(split.length);
          for (String str : split) {
            if ("".equals(str)) {
              continue;
            }

            ret.add(Long.valueOf(str));
          }

          return ret;
        }
      };
}
