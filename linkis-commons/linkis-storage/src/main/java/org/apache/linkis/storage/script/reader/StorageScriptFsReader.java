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

package org.apache.linkis.storage.script.reader;

import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.storage.script.*;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StorageScriptFsReader extends ScriptFsReader {
  private final FsPath path;
  private final String charset;
  private final InputStream inputStream;

  private InputStreamReader inputStreamReader;
  private BufferedReader bufferedReader;

  private ScriptMetaData metadata;

  private List<Variable> variables = new ArrayList<>();
  private String lineText;

  public StorageScriptFsReader(FsPath path, String charset, InputStream inputStream) {
    super(path, charset);
    this.path = path;
    this.charset = charset;
    this.inputStream = inputStream;
  }

  @Override
  public Record getRecord() throws IOException {
    if (metadata == null) throw new IOException("Must read metadata first(必须先读取metadata)");
    ScriptRecord record = new ScriptRecord(lineText);
    lineText = bufferedReader.readLine();
    return record;
  }

  @Override
  public MetaData getMetaData() throws IOException {
    if (metadata == null) init();
    Parser parser = getScriptParser();
    lineText = bufferedReader.readLine();
    while (hasNext()
        && Objects.nonNull(parser)
        && isMetadata(lineText, parser.prefix(), parser.prefixConf())) {
      variables.add(parser.parse(lineText));
      lineText = bufferedReader.readLine();
    }
    metadata = new ScriptMetaData(variables.toArray(new Variable[0]));
    return metadata;
  }

  public void init() {
    inputStreamReader = new InputStreamReader(inputStream);
    bufferedReader = new BufferedReader(inputStreamReader);
  }

  @Override
  public int skip(int recordNum) throws IOException {
    if (recordNum < 0) return -1;
    if (metadata == null) getMetaData();
    try {
      return (int) bufferedReader.skip(recordNum);
    } catch (Throwable t) {
      return recordNum;
    }
  }

  @Override
  public long getPosition() throws IOException {
    return -1L;
  }

  @Override
  public boolean hasNext() throws IOException {
    return lineText != null;
  }

  @Override
  public long available() throws IOException {
    return inputStream != null ? inputStream.available() : 0L;
  }

  @Override
  public void close() throws IOException {
    IOUtils.closeQuietly(bufferedReader);
    IOUtils.closeQuietly(inputStreamReader);
    IOUtils.closeQuietly(inputStream);
  }

  /**
   * Determine if the read line is metadata(判断读的行是否是metadata)
   *
   * @param line
   * @return
   */
  public boolean isMetadata(String line, String prefix, String prefixConf) {
    String regex = "\\s*" + prefix + "\\s*(.+)\\s*=\\s*(.+)\\s*";
    if (line.matches(regex)) {
      return true;
    } else {
      String[] split = line.split("=");
      if (split.length != 2) {
        return false;
      }
      if (Stream.of(split[0].split(" ")).filter(str -> !"".equals(str)).count() != 4) {
        return false;
      }

      Optional<String> optional =
          Stream.of(split[0].split(" ")).filter(str -> !"".equals(str)).findFirst();
      if (optional.isPresent() && !optional.get().equals(prefixConf)) {
        return false;
      }
      return true;
    }
  }

  /**
   * get the script parser according to the path(根据文件路径 获取对应的script parser )
   *
   * @return Scripts Parser
   */
  public Parser getScriptParser() {
    List<Parser> parsers =
        Arrays.stream(ParserFactory.listParsers())
            .filter(p -> p.belongTo(StorageUtils.pathToSuffix(path.getPath())))
            .collect(Collectors.toList());
    if (parsers.size() > 0) {
      return parsers.get(0);
    } else {
      return null;
    }
  }
}
