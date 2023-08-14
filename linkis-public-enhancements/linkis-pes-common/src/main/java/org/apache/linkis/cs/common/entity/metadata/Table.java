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

package org.apache.linkis.cs.common.entity.metadata;

import java.util.Date;
import java.util.List;

public interface Table {

  String getName();

  void setName(String name);

  String getAlias();

  void setAlias(String alias);

  String getCreator();

  void setCreator(String creator);

  String getComment();

  void setComment(String comment);

  Date getCreateTime();

  void setCreateTime(Date createTime);

  String getProductName();

  void setProductName(String productName);

  String getProjectName();

  void setProjectName(String projectName);

  String getUsage();

  void setUsage(String usage);

  Integer getLifecycle();

  void setLifecycle(Integer lifecycle);

  Integer getUseWay();

  void setUseWay(Integer useWay);

  Boolean getImport();

  void setImport(Boolean anImport);

  Integer getModelLevel();

  void setModelLevel(Integer modelLevel);

  Boolean getExternalUse();

  void setExternalUse(Boolean externalUse);

  Boolean getPartitionTable();

  void setPartitionTable(Boolean partitionTable);

  Boolean getAvailable();

  void setAvailable(Boolean available);

  Boolean getView();

  CSDB getDb();

  void setDb(CSDB db);

  void setView(Boolean view);

  CSColumn[] getColumns();

  void setColumns(CSColumn[] columns);

  List<CSPartition> getPartitions();

  void setPartitions(List<CSPartition> partitions);
}
