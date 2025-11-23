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

package org.apache.linkis.gateway.authentication.conf

import org.apache.linkis.common.conf.CommonVars

object TokenConfiguration {
  val TOKEN_WILD_CHAR: String = "*"

  /**
   * -1 for last forever
   */
  val TOKEN_ELAPSE_TIME_DEFAULT: Int =
    CommonVars[Int]("wds.linkis.token.elapse.time", -1).getValue

  /**
   * Token Cache
   */
  val TOKEN_CACHE_MAX_SIZE: Int =
    CommonVars[Int]("wds.linkis.token.cache.max.size", 5000).getValue

  val TOKEN_CACHE_EXPIRE_MINUTES: Int =
    CommonVars[Int]("wds.linkis.token.cache.expire.minutes", 2).getValue

}
