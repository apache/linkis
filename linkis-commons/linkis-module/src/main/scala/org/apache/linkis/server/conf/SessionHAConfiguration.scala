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

package org.apache.linkis.server.conf

import org.apache.linkis.common.conf.CommonVars

object SessionHAConfiguration {

  val RedisHost: String = CommonVars("linkis.session.redis.host", "127.0.0.1").getValue
  val RedisPort: Int = CommonVars("linkis.session.redis.port", 6379).getValue
  val RedisPassword: String = CommonVars("linkis.session.redis.password", "").getValue

  val RedisSentinalMaster: String =
    CommonVars("linkis.session.redis.sentinel.master", "").getValue

  val RedisSentinalServer: String = CommonVars("linkis.session.redis.sentinel.nodes", "").getValue

  val SsoRedis: Boolean = CommonVars("linkis.session.redis.cache.enabled", false).getValue
}
