/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.metadata.hive.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
/**
 * Created by shanhuang on 9/13/18.
 */
public interface HiveMetaDao {
    String getLocationByDbAndTable(Map<String, String> map);
    List<String> getDbsByUser(String userName);
    List<Map<String, Object>> getTablesByDbNameAndUser(Map<String, String> map);
    Long getPartitionSize(Map<String, String> map);
    List<String> getPartitions(Map<String, String> map);
    List<Map<String, Object>> getColumns(Map<String, String> map);
    List<Map<String, Object>> getPartitionKeys(Map<String, String> map);
    String getTableComment(@Param("DbName") String DbName, @Param("tableName") String tableName);
}
