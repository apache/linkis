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

package com.webank.wedatasphere.linkis.metadatamanager.service;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * Configuration
 * @author davidhua
 * 2020/02/14
 */
public class ElasticParamsMapper {
    public static final CommonVars<String> PARAM_ES_URLS =
            CommonVars.apply("wds.linkis.server.mdm.service.es.urls", "elasticUrls");

    public static final CommonVars<String> PARAM_ES_USERNAME =
            CommonVars.apply("wds.linkis.server.mdm.service.es.username", "username");

    public static final CommonVars<String> PARAM_ES_PASSWORD =
            CommonVars.apply("wds.linkis.server.mdm.service.es.password", "password");
}
