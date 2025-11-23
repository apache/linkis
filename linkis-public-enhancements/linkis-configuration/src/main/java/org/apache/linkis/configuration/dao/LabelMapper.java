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

package org.apache.linkis.configuration.dao;

import org.apache.linkis.configuration.entity.ConfigLabel;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LabelMapper {

  ConfigLabel getLabelByKeyValue(
      @Param("labelKey") String labelKey, @Param("stringValue") String stringValue);

  // label key:combined_userCreator_engineType
  List<ConfigLabel> selectUserCreatorEngineTypeLabelList(@Param("itemList") List<String> itemList);

  void insertLabel(ConfigLabel label);

  void batchInsertLabel(@Param("labelList") List<ConfigLabel> labelList);

  void deleteLabel(@Param("ids") List<Integer> ids);

  ConfigLabel getLabelById(@Param("id") Integer id);
}
