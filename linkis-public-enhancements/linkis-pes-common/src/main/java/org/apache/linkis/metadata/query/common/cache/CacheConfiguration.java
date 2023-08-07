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

package org.apache.linkis.metadata.query.common.cache;

import org.apache.linkis.common.conf.CommonVars;

public class CacheConfiguration {

  public static CommonVars<Long> CACHE_MAX_SIZE =
      CommonVars.apply("wds.linkis.server.mdm.service.cache.max-size", 1000L);

  public static CommonVars<Long> CACHE_EXPIRE_TIME =
      CommonVars.apply("wds.linkis.server.mdm.service.cache.expire", 600L);

  /** Make a pool for each cache element */
  public static final CommonVars<Integer> CACHE_IN_POOL_SIZE =
      CommonVars.apply("wds.linkis.server.mdm.service.cache.in-pool.size", 5);

  public static final CommonVars<String> MYSQL_RELATIONSHIP_LIST =
      CommonVars.apply(
          "wds.linkis.server.mdq.mysql.relationship",
          "mysql,oracle,kingbase,postgresql,sqlserver,db2,greenplum,dm,doris,clickhouse,tidb,starrocks,gaussdb,oceanbase");

  public static final CommonVars<String> QUERY_DATABASE_RELATIONSHIP =
      CommonVars.apply(
          "linkis.server.mdq.query.database.relationship",
          "{\"tidb\":\"mysql\",\"doris\":\"mysql\",\"starrocks\":\"mysql\",\"oceanbase\":\"mysql\",\"gaussdb\":\"postgresql\"}");
}
