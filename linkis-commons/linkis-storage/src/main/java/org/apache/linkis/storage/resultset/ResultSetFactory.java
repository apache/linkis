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

package org.apache.linkis.storage.resultset;

import org.apache.linkis.common.io.*;
import org.apache.linkis.common.io.resultset.ResultSet;

import java.util.LinkedHashMap;
import java.util.Map;

public interface ResultSetFactory {

  String TEXT_TYPE = "1";
  String TABLE_TYPE = "2";
  String IO_TYPE = "3";
  String PICTURE_TYPE = "4";
  String HTML_TYPE = "5";

  /** TODO 修改为注册形式，并修改ResultSet的getResultType逻辑 Result set corresponding type record(结果集对应类型记录) */
  Map<String, String> resultSetType =
      new LinkedHashMap<String, String>() {
        {
          put(TEXT_TYPE, "TEXT");
          put(TABLE_TYPE, "TABLE");
          put(IO_TYPE, "IO");
          put(PICTURE_TYPE, "PICTURE");
          put(HTML_TYPE, "HTML");
        }
      };

  DefaultResultSetFactory factory = new DefaultResultSetFactory();

  static ResultSetFactory getInstance() {
    return factory;
  }

  ResultSet<? extends MetaData, ? extends Record> getResultSetByType(String resultSetType);

  ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(FsPath fsPath);

  ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(FsPath fsPath, Fs fs);

  ResultSet<? extends MetaData, ? extends Record> getResultSetByContent(String content);

  boolean exists(String resultSetType);

  boolean isResultSetPath(String path);

  boolean isResultSet(String content);

  ResultSet<? extends MetaData, ? extends Record> getResultSet(String output);

  ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(
      FsPath fsPath, String proxyUser);

  ResultSet<? extends MetaData, ? extends Record> getResultSet(String output, String proxyUser);

  String[] getResultSetType();
}
