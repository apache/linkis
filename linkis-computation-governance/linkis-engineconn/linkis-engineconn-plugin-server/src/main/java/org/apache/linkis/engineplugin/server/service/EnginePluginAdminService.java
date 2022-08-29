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

package org.apache.linkis.engineplugin.server.service;

import org.apache.linkis.bml.protocol.Version;
import org.apache.linkis.engineplugin.server.entity.EngineConnBmlResource;
import org.apache.linkis.engineplugin.vo.EnginePluginBMLVo;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import com.github.pagehelper.PageInfo;

public interface EnginePluginAdminService {

  void rollBackEnginePlugin(EngineConnBmlResource engineConnBmlResource);

  void uploadToECHome(MultipartFile file);

  void deleteEnginePluginBML(String ecType, String version, String username);

  PageInfo<EngineConnBmlResource> queryDataSourceInfoPage(EnginePluginBMLVo enginePluginBMLVo);

  List<String> getTypeList();

  List<String> getTypeVersionList(String type);

  List<Version> getVersionList(String userName, String bmlResourceId);
}
