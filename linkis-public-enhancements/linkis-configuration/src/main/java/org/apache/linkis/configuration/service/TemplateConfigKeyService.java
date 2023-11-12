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

package org.apache.linkis.configuration.service;

import org.apache.linkis.configuration.entity.ConfigKeyLimitVo;
import org.apache.linkis.configuration.exception.ConfigurationException;
import org.apache.linkis.governance.common.protocol.conf.TemplateConfRequest;
import org.apache.linkis.governance.common.protocol.conf.TemplateConfResponse;

import java.util.List;
import java.util.Map;

public interface TemplateConfigKeyService {

  Boolean updateKeyMapping(
      String templateUid,
      String templateName,
      String engineType,
      String operator,
      List<ConfigKeyLimitVo> itemList,
      Boolean isFullMode)
      throws ConfigurationException;

  List<Object> queryKeyInfoList(List<String> uuidList) throws ConfigurationException;

  Map<String, Object> apply(
      String templateUid,
      String application,
      String engineType,
      String engineVersion,
      String operator,
      List<String> userList)
      throws ConfigurationException;

  TemplateConfResponse queryKeyInfoList(TemplateConfRequest templateConfRequest);
}
