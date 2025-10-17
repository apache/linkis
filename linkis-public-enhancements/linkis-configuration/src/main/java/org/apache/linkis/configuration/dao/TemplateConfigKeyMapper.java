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

import org.apache.linkis.configuration.entity.TemplateConfigKey;
import org.apache.linkis.configuration.entity.TemplateConfigKeyVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/** The dao interface class of the linkis_ps_configuration_template_config_key table @Description */
public interface TemplateConfigKeyMapper {

  int batchInsertList(List<TemplateConfigKey> list);

  List<TemplateConfigKey> selectListByTemplateUuid(@Param("templateUuid") String templateUuid);

  int deleteByTemplateUuidAndKeyIdList(
      @Param("templateUuid") String templateUuid, @Param("keyIdList") List<Long> KeyIdList);

  int batchInsertOrUpdateList(List<TemplateConfigKey> list);

  List<TemplateConfigKey> selectListByTemplateUuidList(
      @Param("templateUuidList") List<String> templateUuidList);

  List<TemplateConfigKeyVO> selectInfoListByTemplateUuid(
      @Param("templateUuid") String templateUuid);

  List<TemplateConfigKeyVO> selectInfoListByTemplateName(
      @Param("templateName") String templateName);

  List<String> selectEngineTypeByTemplateUuid(@Param("templateUuid") String templateUuid);

  List<TemplateConfigKey> selectListByKeyId(@Param("keyId") Long keyId);

  void updateConfigValue(TemplateConfigKey confKey);
}
