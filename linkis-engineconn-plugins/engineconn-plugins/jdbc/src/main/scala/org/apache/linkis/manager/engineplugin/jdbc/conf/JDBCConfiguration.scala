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
 
package org.apache.linkis.manager.engineplugin.jdbc.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object JDBCConfiguration {

  val ENGINE_RESULT_SET_MAX_CACHE = CommonVars("wds.linkis.resultSet.cache.max", new ByteType("0k"))

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.jdbc.default.limit", 5000)

  val JDBC_QUERY_TIMEOUT = CommonVars("wds.linkis.jdbc.query.timeout", 1800)

  val JDBC_SUPPORT_DBS = CommonVars("wds.linkis.jdbc.support.dbs", "mysql=>com.mysql.jdbc.Driver,postgresql=>org.postgresql.Driver,oracle=>oracle.jdbc.driver.OracleDriver,hive2=>org.apache.hive.jdbc.HiveDriver,presto=>com.facebook.presto.jdbc.PrestoDriver")

  val JDBC_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.jdbc.concurrent.limit", 100)
}
