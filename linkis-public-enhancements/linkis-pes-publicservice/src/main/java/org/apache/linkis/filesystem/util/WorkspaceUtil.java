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

package org.apache.linkis.filesystem.util;

import org.apache.linkis.filesystem.entity.LogLevel;
import org.apache.linkis.filesystem.exception.WorkSpaceException;
import org.apache.linkis.filesystem.exception.WorkspaceExceptionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

public class WorkspaceUtil {

  public static String infoReg =
      "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
          + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]"
          + "\\.\\d{3}\\s*INFO(.*)";
  public static String warnReg =
      "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
          + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]"
          + "\\.\\d{3}\\s*WARN(.*)";
  public static String errorReg =
      "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
          + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]"
          + "\\.\\d{3}\\s*ERROR(.*)";
  public static String allReg = "(.*?)";

  public static List<LogLevel.Type> logReg = new ArrayList<>();

  public static List<Integer> logMatch(String code, LogLevel logLevel) {
    if (logReg.isEmpty()) {
      synchronized (WorkspaceUtil.class) {
        if (logReg.isEmpty()) {
          logReg.add(LogLevel.Type.ERROR);
          logReg.add(LogLevel.Type.WARN);
          logReg.add(LogLevel.Type.INFO);
        }
      }
    }
    ArrayList<Integer> result = new ArrayList<>();
    Optional<LogLevel.Type> any =
        logReg.stream().filter(r -> Pattern.matches(r.getReg(), code)).findAny();
    if (any.isPresent()) {
      result.add(any.get().ordinal());
      result.add(LogLevel.Type.ALL.ordinal());
      logLevel.setType(any.get());
    } else {
      result.add(LogLevel.Type.ALL.ordinal());
      if (logLevel.getType() != LogLevel.Type.ALL) {
        result.add(logLevel.getType().ordinal());
      }
    }
    return result;
  }

  public static Function<String, String> suffixTuningFunction =
      p -> {
        if (p.endsWith(File.separator)) return p;
        else return p + File.separator;
      };

  public static String suffixTuning(String path) {
    return suffixTuningFunction.apply(path);
  }

  public static void fileAndDirNameSpecialCharCheck(String path) throws WorkSpaceException {
    String name = new File(path).getName();
    charCheckFileName(name);
  }

  public static void charCheckFileName(String fileName) throws WorkSpaceException {
    int i = fileName.lastIndexOf(".");
    if (i != -1) {
      fileName = fileName.substring(0, i);
    }
    // Only support numbers, uppercase letters, underscores, Chinese(只支持数字,字母大小写,下划线,中文)
    String specialRegEx = "^[\\w\\u4e00-\\u9fa5]{1,200}$";
    Pattern specialPattern = Pattern.compile(specialRegEx);
    if (!specialPattern.matcher(fileName).find()) {
      WorkspaceExceptionManager.createException(80028);
    }
  }
}
