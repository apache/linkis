/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.resourcemanager.external.service;

import org.apache.linkis.manager.common.entity.resource.NodeResource;
import org.apache.linkis.manager.common.entity.resource.ResourceType;
import org.apache.linkis.resourcemanager.domain.RMLabelContainer;
import org.apache.linkis.resourcemanager.exception.RMErrorException;
import org.apache.linkis.resourcemanager.external.domain.ExternalAppInfo;
import org.apache.linkis.resourcemanager.external.domain.ExternalResourceIdentifier;

import java.util.List;
import java.util.Map;

public interface ExternalResourceService {

    NodeResource getResource(ResourceType resourceType, RMLabelContainer labelContainer, Map<String, Object> identifierMap) throws RMErrorException;
    NodeResource getResource(ResourceType resourceType, RMLabelContainer labelContainer, ExternalResourceIdentifier identifier) throws RMErrorException;
    List<ExternalAppInfo> getAppInfo(ResourceType resourceType, RMLabelContainer labelContainer, Map<String, Object> identifierMap) throws RMErrorException;
    List<ExternalAppInfo> getAppInfo(ResourceType resourceType, RMLabelContainer labelContainer, ExternalResourceIdentifier identifier) throws RMErrorException;
}
