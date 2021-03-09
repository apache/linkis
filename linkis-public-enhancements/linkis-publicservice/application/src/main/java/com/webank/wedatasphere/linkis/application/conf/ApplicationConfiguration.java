/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.application.conf;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;

/**
 * Created by johnnwang on 2019/1/18.
 */
public class ApplicationConfiguration {

    public static final CommonVars JSON_ROOT_PATH = CommonVars$.MODULE$.apply("wds.linkis.application.json.rootpath","hdfs:///tmp/");
    public static final CommonVars DEFAULT_APPLICATIO_NAME = CommonVars$.MODULE$.apply("wds.linkis.application.default.name","默认应用");
    public static final CommonVars ISASH_LEVEL = CommonVars$.MODULE$.apply("wds.linkis.application.ash.level",4);
    public static final CommonVars INIT_ORGID = CommonVars$.MODULE$.apply("wds.linkis.application.init.orgid",3);
    public static final CommonVars DWS_PARAMS = CommonVars$.MODULE$.apply("wds.linkis.application.dws.params","");
}
