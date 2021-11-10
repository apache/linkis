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
 
package org.apache.linkis.datasourcemanager.core.service;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.WarnException;

import java.util.Map;

/**
 * Metadata service
 */
public interface MetadataOperateService {

    /**
     * Build connection with  parameters in request
     * @param mdRemoteServiceName metadata remote service
     * @param operator operate user
     * @param connectParams parameters
     * @throws ErrorException
     */
    void doRemoteConnect(String mdRemoteServiceName, String operator, Map<String, Object> connectParams) throws WarnException;
}
