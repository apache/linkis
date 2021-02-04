/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.datasourcemanager.core.service;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.common.exception.WarnException;

import java.util.Map;

/**
 * Metadata service
 * @author davidhua
 * 2020/02/14
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
