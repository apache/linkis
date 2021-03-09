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

package com.webank.wedatasphere.linkis.metadatamanager.common;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;

/**
 * Created by jackyxxie on 2020/2/10.
 */
public class MdmConfiguration {

    public static CommonVars<String> METADATA_SERVICE_APPLICATION =
            CommonVars.apply("wds.linkis.server.mdm.service.app.name", "mdm-service");

    public static CommonVars<String> DATA_SOURCE_SERVICE_APPLICATION =
            CommonVars.apply("wds.linkis.server.dsm.app.name", "dsm-server");
}
