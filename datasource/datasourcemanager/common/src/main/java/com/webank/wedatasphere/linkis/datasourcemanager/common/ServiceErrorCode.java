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

package com.webank.wedatasphere.linkis.datasourcemanager.common;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * Store error code map
 * @author kirkzhou
 * 2020/02/11
 */
public class ServiceErrorCode {

    public static CommonVars<Integer> TRANSFORM_FORM_ERROR =
            CommonVars.apply("wds.linkis.server.dsm.error-code.transform", 99987);

    public static CommonVars<Integer> BML_SERVICE_ERROR =
            CommonVars.apply("wds.linkis.server.dsm.error-code.bml", 99982);

    public static CommonVars<Integer> REMOTE_METADATA_SERVICE_ERROR =
            CommonVars.apply("wds.linkis.server.dsm.error-code.metadata", 99983);

    public static CommonVars<Integer> PARAM_VALIDATE_ERROR =
            CommonVars.apply("wds.linkis.server.dsm.error-code.param-validate", 99986);
}
