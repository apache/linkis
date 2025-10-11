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

package org.apache.linkis.metadata.service;

import org.apache.linkis.metadata.hive.dto.MetadataQueryParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

public interface DataSourceService {

  JsonNode getDbs(String userName, String permission) throws Exception;

  Set<String> getRangerDbs(String username) throws Exception;

  Set<String> getHiveDbs(String userName, String permission) throws Exception;

  Boolean checkRangerConnectionConfig();

  JsonNode getDbsWithTables(String userName) throws Exception;

  JsonNode getDbsWithTablesAndLastAccessAt(String userName) throws Exception;

  JsonNode queryTables(MetadataQueryParam queryParam);

  List<Map<String, Object>> queryHiveTables(MetadataQueryParam queryParam);

  List<String> queryRangerTables(MetadataQueryParam queryParam);

  List<String> getRangerColumns(MetadataQueryParam queryParam);

  JsonNode filterRangerColumns(JsonNode hiveColumns, List<String> rangerColumns);

  JsonNode queryTablesWithLastAccessAt(MetadataQueryParam queryParam);

  JsonNode queryTableMeta(MetadataQueryParam queryParam);

  JsonNode queryTableMetaBySDID(MetadataQueryParam queryParam);

  JsonNode getTableSize(MetadataQueryParam queryParam);

  JsonNode getPartitionSize(MetadataQueryParam queryParam);

  JsonNode getPartitions(MetadataQueryParam queryParam);

  boolean partitionExists(MetadataQueryParam queryParam);

  Map<String, Object> getStorageInfo(MetadataQueryParam queryParam);
}
