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

package org.apache.linkis.udf.dao;

import org.apache.linkis.udf.entity.UDFTree;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UDFTreeDao {

  void addTree(UDFTree udfTree);

  void updateTree(UDFTree udfTree);

  void deleteTree(Long id, String userName);

  UDFTree getTreeByIdAndCategory(Long id, String category);

  UDFTree getTreeByIdAndCategoryAndUserName(Long id, String category, String userName);

  UDFTree getTreeById(@Param("id") Long id);

  List<UDFTree> getTreesByParentId(Map<String, Object> params);

  UDFTree getTreeByNameAndUser(
      @Param("treeName") String treeName,
      @Param("userName") String userName,
      @Param("category") String category);

  List<String> getUserDirectory(
      @Param("userName") String userName, @Param("category") String category);
}
