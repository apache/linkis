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

package org.apache.linkis.variable.dao;

import org.apache.linkis.variable.entity.VarKey;
import org.apache.linkis.variable.entity.VarKeyUser;
import org.apache.linkis.variable.entity.VarKeyValueVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface VarMapper {

  List<VarKeyValueVO> listGlobalVariable(@Param("userName") String userName);

  VarKeyUser getValueByKeyID(@Param("keyID") Long keyID);

  void removeKey(@Param("keyID") Long keyID);

  void removeValue(@Param("valueID") Long valueID);

  void insertKey(VarKey varKey);

  void insertValue(VarKeyUser varKeyUser);

  void updateValue(@Param("valueID") Long valueID, @Param("value") String value);
}
