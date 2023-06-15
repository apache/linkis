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

package org.apache.linkis.cli.application.interactor.job.common;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class ResultSet implements Cloneable {
  private int resultsetIdx;
  private List<LinkedHashMap<String, String>> resultMeta;
  private List<List<String>> content;

  public ResultSet() {}

  public int getResultsetIdx() {
    return resultsetIdx;
  }

  public void setResultsetIdx(int resultsetIdx) {
    this.resultsetIdx = resultsetIdx;
  }

  public List<LinkedHashMap<String, String>> getResultMeta() {
    return resultMeta;
  }

  public void setResultMeta(List<LinkedHashMap<String, String>> resultMeta) {
    this.resultMeta = resultMeta;
  }

  public List<List<String>> getContent() {
    return content;
  }

  public void setContent(List<List<String>> content) {
    this.content = content;
  }

  @Override
  protected ResultSet clone() throws CloneNotSupportedException {
    ResultSet ret = new ResultSet();
    if (this.resultMeta != null) {
      List<LinkedHashMap<String, String>> resultMeta = null;
      ret.resultMeta = new LinkedList<>();
      for (LinkedHashMap<String, String> r1 : resultMeta) {
        ret.resultMeta.add((LinkedHashMap<String, String>) r1.clone());
      }
    }
    if (this.content.size() != 0) {
      ret.content = new LinkedList<>();
      for (List<String> r1 : content) {
        ret.content.add(new LinkedList<>(r1));
      }
    }
    return ret;
  }
}
