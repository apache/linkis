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

package org.apache.linkis.cs.common.serialize.impl.history.metadata;

import org.apache.linkis.cs.common.entity.history.metadata.CSTableMetadataContextHistory;
import org.apache.linkis.cs.common.entity.history.metadata.TableOperationType;
import org.apache.linkis.cs.common.entity.metadata.CSTable;
import org.apache.linkis.cs.common.entity.metadata.Table;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.common.serialize.AbstractSerializer;
import org.apache.linkis.cs.common.serialize.impl.history.CommonHistorySerializer;
import org.apache.linkis.cs.common.utils.CSCommonUtils;

import java.util.Map;

public class CSTableMetadataHistorySerializer
    extends AbstractSerializer<CSTableMetadataContextHistory> implements CommonHistorySerializer {

  @Override
  public CSTableMetadataContextHistory fromJson(String json) throws CSErrorException {
    Map<String, String> map = getMapValue(json);
    CSTableMetadataContextHistory history = get(map, new CSTableMetadataContextHistory());
    history.setTable(CSCommonUtils.gson.fromJson(map.get("table"), CSTable.class));
    history.setOperationType(TableOperationType.valueOf(map.get("operationType")));
    return history;
  }

  @Override
  public String getJsonValue(CSTableMetadataContextHistory tableHistory) throws CSErrorException {
    Table table = tableHistory.getTable();
    String tableStr = CSCommonUtils.gson.toJson(table);
    Map<String, String> mapValue = getMapValue(tableHistory);
    mapValue.put("table", tableStr);
    mapValue.put("operationType", tableHistory.getOperationType().name());
    return CSCommonUtils.gson.toJson(mapValue);
  }

  @Override
  public String getType() {
    return "CSTableMetadataContextHistory";
  }

  @Override
  public boolean accepts(Object obj) {
    return null != obj
        && obj.getClass().getName().equals(CSTableMetadataContextHistory.class.getName());
  }
}
