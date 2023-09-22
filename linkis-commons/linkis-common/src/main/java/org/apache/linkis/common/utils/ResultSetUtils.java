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

package org.apache.linkis.common.utils;

import org.apache.linkis.common.io.FsPath;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultSetUtils {

  // Sort in ASC order by numx in the result set _numx.dolphin file name
  public static Comparator<FsPath> getResultSetFileComparatorOrderByNameNum() {

    Comparator<FsPath> comparator =
        (o1, o2) -> {
          // get the num of file name
          String regx = "\\d+";

          String[] res1 = o1.getPath().split(File.separator);
          String fileName1 = res1[res1.length - 1];
          Matcher matcher1 = Pattern.compile(regx).matcher(fileName1);
          int num1 = matcher1.find() ? Integer.parseInt(matcher1.group()) : Integer.MAX_VALUE;

          String[] res2 = o2.getPath().split(File.separator);
          String fileName2 = res2[res2.length - 1];
          Matcher matcher2 = Pattern.compile(regx).matcher(fileName2);
          int num2 = matcher2.find() ? Integer.parseInt(matcher2.group()) : Integer.MAX_VALUE;

          return num1 - num2;
        };
    return comparator;
  }

  public static void sortByNameNum(List<FsPath> fsPathList) {
    Collections.sort(fsPathList, getResultSetFileComparatorOrderByNameNum());
  }
}
