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

import org.apache.linkis.storage.LineRecord;
import org.apache.linkis.storage.script.ScriptRecord;

import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class TextFileSource extends AbstractFileSource {
  public static final String[] LINE_BREAKER = new String[] {"\n"};

  public TextFileSource(FileSplit[] fileSplits) {
    super(fileSplits);
    shuffle(
        record -> {
          if (record instanceof ScriptRecord && "".equals(((ScriptRecord) record).getLine())) {
            return new LineRecord("\n");
          } else {
            return record;
          }
        });
  }

  @Override
  public Pair<Object, List<String[]>>[] collect() {
    Pair<Object, List<String[]>>[] collects = super.collect();
    if (!getParams().getOrDefault("ifMerge", "true").equals("true")) {
      return collects;
    }
    ArrayList<List<String[]>> snds =
        Arrays.stream(collects)
            .map(Pair::getSecond)
            .collect(Collectors.toCollection(ArrayList::new));
    snds.forEach(
        snd -> {
          StringBuilder str = new StringBuilder();
          snd.forEach(
              arr -> {
                if (Arrays.equals(arr, LINE_BREAKER)) {
                  str.append("\n");
                } else {
                  str.append(arr[0]).append("\n");
                }
              });
          snd.clear();
          snd.add(new String[] {str.toString()});
        });
    return collects;
  }
}
