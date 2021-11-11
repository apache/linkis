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
 
package org.apache.linkis.cs.highavailable.conf;

import org.apache.linkis.common.conf.CommonVars;

public class ContextHighAvailableConf {

    public static CommonVars<Boolean> ENABLE_STRICT_HAID_CHECK = CommonVars.apply("wds.linkis.cs.haid.strict_check.enable", false);

    public static CommonVars<Long> CS_ALIAS_CACHE_EXPIRE_TIMEMILLS = CommonVars.apply("wds.linkis.cs.alias.cache.expire.mills", 2000L * 3600);

    public static CommonVars<String> CONTEXTSERVICE_PREFIX = CommonVars.apply("wds.linkis.cs.ha.route_label.prefix", "cs_");
}
