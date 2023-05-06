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

package org.apache.linkis.storage;

import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.storage.resultset.ResultMetaData;

public class LineMetaData implements ResultMetaData {

  private String metaData = null;

  public LineMetaData() {}

  public LineMetaData(String metaData) {
    this.metaData = metaData;
  }

  public String getMetaData() {
    return metaData;
  }

  public void setMetaData(String metaData) {
    this.metaData = metaData;
  }

  public MetaData cloneMeta() {
    return new LineMetaData(metaData);
  }
}
