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

package org.apache.linkis.basedatamanager.server.service;

import org.apache.linkis.basedatamanager.server.domain.ConfigurationConfigKey;
import org.apache.linkis.basedatamanager.server.request.ConfigurationTemplateSaveRequest;
import org.apache.linkis.basedatamanager.server.response.EngineLabelResponse;

import java.util.List;

/** This module is designed to manage configuration parameter templates */
public interface ConfigurationTemplateService {

  /**
   * save a configuration template
   *
   * @param request ConfigurationTemplateSaveRequest
   * @return Boolean
   */
  Boolean saveConfigurationTemplate(ConfigurationTemplateSaveRequest request);

  /**
   * delete a configuration template
   *
   * @param keyId Long
   * @return Boolean
   */
  Boolean deleteConfigurationTemplate(Long keyId);

  /**
   * query all engine list
   *
   * @return List
   */
  List<EngineLabelResponse> getEngineList();

  /**
   * query config key list by label id
   *
   * @param engineLabelId engine label id
   * @return List
   */
  List<ConfigurationConfigKey> getTemplateListByLabelId(String engineLabelId);
}
