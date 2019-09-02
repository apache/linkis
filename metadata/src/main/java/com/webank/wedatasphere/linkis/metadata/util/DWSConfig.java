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

package com.webank.wedatasphere.linkis.metadata.util;

import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.conf.CommonVars$;
/**
 * Created by shanhuang on 9/13/18.
 */
public class DWSConfig {

    public static CommonVars<String> IDE_URL = CommonVars$.MODULE$.apply("wds.linkis.ide.url", "locahost");
    public static CommonVars<String> HADOOP_CONF_DIR = CommonVars$.MODULE$.apply("hadoop.config.dir",
            CommonVars$.MODULE$.apply("HADOOP_CONF_DIR", "/appcom/config/hadoop-config").getValue());
    public static CommonVars<String> HIVE_CONF_DIR = CommonVars$.MODULE$.apply("hive.config.dir",
            CommonVars$.MODULE$.apply("HIVE_CONF_DIR", "/appcom/config/hadoop-config").getValue());
    public static CommonVars<String> HIVE_META_URL = CommonVars$.MODULE$.apply("hive.meta.url", "");
    public static CommonVars<String> HIVE_META_USER = CommonVars$.MODULE$.apply("hive.meta.user", "");
    public static CommonVars<String> HIVE_META_PASSWORD = CommonVars$.MODULE$.apply("hive.meta.password", "");
}
