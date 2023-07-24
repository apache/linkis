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

package org.apache.linkis.storage.source;

import org.apache.linkis.common.io.FsWriter;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractFileSource implements FileSource {

  private FileSplit[] fileSplits;

  public AbstractFileSource(FileSplit[] fileSplits) {
    this.fileSplits = fileSplits;
  }

  @Override
  public FileSource shuffle(Function<Record, Record> function) {
    Arrays.stream(fileSplits).forEach(fileSplit -> fileSplit.shuffler = function);
    return this;
  }

  @Override
  public FileSource page(int page, int pageSize) {
    Arrays.stream(fileSplits).forEach(fileSplit -> fileSplit.page(page, pageSize));
    return this;
  }

  @Override
  public FileSource addParams(Map<String, String> params) {
    Arrays.stream(fileSplits).forEach(fileSplit -> fileSplit.addParams(params));
    return this;
  }

  @Override
  public FileSource addParams(String key, String value) {
    Arrays.stream(fileSplits).forEach(fileSplit -> fileSplit.addParams(key, value));
    return this;
  }

  @Override
  public FileSplit[] getFileSplits() {
    return this.fileSplits;
  }

  @Override
  public Map<String, String> getParams() {
    return Arrays.stream(fileSplits)
        .map(FileSplit::getParams)
        .flatMap(map -> map.entrySet().stream())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (existingValue, newValue) -> newValue));
  }

  @Override
  public <K extends MetaData, V extends Record> void write(FsWriter<K, V> fsWriter) {
    Arrays.stream(fileSplits).forEach(fileSplit -> fileSplit.write(fsWriter));
  }

  @Override
  public void close() {
    Arrays.stream(fileSplits).forEach(IOUtils::closeQuietly);
  }

  @Override
  public Pair<Object, List<String[]>>[] collect() {
    return Arrays.stream(fileSplits).map(FileSplit::collect).toArray(Pair[]::new);
  }

  @Override
  public int getTotalLine() {
    return Arrays.stream(fileSplits).mapToInt(FileSplit::getTotalLine).sum();
  }

  @Override
  public String[] getTypes() {
    return Arrays.stream(fileSplits).map(FileSplit::getType).toArray(String[]::new);
  }

  @Override
  public Pair<Integer, Integer>[] getFileInfo(int needToCountRowNumber) {
    return Arrays.stream(fileSplits)
        .map(fileSplit -> fileSplit.getFileInfo(needToCountRowNumber))
        .toArray(Pair[]::new);
  }
}
