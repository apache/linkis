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

package org.apache.linkis.metadata.query.service.conf;

import org.apache.linkis.common.conf.CommonVars;

public class SqlParamsMapper {
  public static final CommonVars<String> PARAM_SQL_HOST =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.host", "host");

  public static final CommonVars<String> PARAM_SQL_PORT =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.port", "port");

  public static final CommonVars<String> PARAM_SQL_USERNAME =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.username", "username");

  public static final CommonVars<String> PARAM_SQL_PASSWORD =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.password", "password");

  public static final CommonVars<String> PARAM_SQL_DATABASE =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.instance", "instance");

  public static final CommonVars<String> PARAM_SQL_EXTRA_PARAMS =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.params", "params");

  public static final CommonVars<String> PARAM_SQL_SERVICE_NAME =
      CommonVars.apply("wds.linkis.server.mdm.service.sql.service.name", "servicename");
}
