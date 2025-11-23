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

package org.apache.linkis.manager.engineplugin.jdbc.conf

import org.apache.linkis.common.conf.{ByteType, CommonVars}

object JDBCConfiguration {

  val ENGINE_RESULT_SET_MAX_CACHE =
    CommonVars("wds.linkis.resultSet.cache.max", new ByteType("0k"))

  val ENGINE_DEFAULT_LIMIT = CommonVars("wds.linkis.jdbc.default.limit", 5000)

  val JDBC_QUERY_TIMEOUT = CommonVars("wds.linkis.jdbc.query.timeout", 1800)

  val JDBC_CONCURRENT_LIMIT = CommonVars[Int]("wds.linkis.engineconn.jdbc.concurrent.limit", 100)

  val JDBC_KERBEROS_ENABLE = CommonVars[Boolean]("wds.linkis.keytab.enable", false)

  val CHANGE_DS_TYPE_TO_MYSQL =
    CommonVars[String]("linkis.change.ds.type.to.mysql", "starrocks").getValue

  val NOT_SUPPORT_LIMIT_DBS = CommonVars[String]("linkis.not.support.limit.dbs", "oracle").getValue

  val DS_TYPES_TO_EXECUTE_TASK_BY_JDBC =
    CommonVars[String]("linkis.can.execute.task.ds.types.by.jdbc", "starrocks").getValue

  val SUPPORT_CONN_PARAM_EXECUTE_ENABLE: Boolean =
    CommonVars[Boolean]("linkis.support.conn.param.execute.enable", true).getValue

}
