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

import org.apache.linkis.udf.entity.PythonModuleInfo;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PythonModuleInfoMapper {

  // SQL 1: 模糊查询
  List<PythonModuleInfo> selectByConditions(PythonModuleInfo pythonModuleInfo);

  // SQL 2: 更新
  int updatePythonModuleInfo(PythonModuleInfo pythonModuleInfo);

  // SQL 3: 新增
  Long insertPythonModuleInfo(PythonModuleInfo pythonModuleInfo);

  // SQL 4: 带有<if>判断的查询
  PythonModuleInfo selectByUserAndNameAndId(PythonModuleInfo pythonModuleInfo);

  // SQL 5: 查询包含多个引擎类型的hdfs路径
  List<PythonModuleInfo> selectPathsByUsernameAndEnginetypes(
      String username, List<String> enginetypes);
}
