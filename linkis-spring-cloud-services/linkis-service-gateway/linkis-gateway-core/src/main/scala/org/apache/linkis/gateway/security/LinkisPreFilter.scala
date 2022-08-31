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

package org.apache.linkis.gateway.security

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.gateway.http.GatewayContext

trait LinkisPreFilter {

  def doFilter(gatewayContext: GatewayContext): Boolean

  def name: String
}

object LinkisPreFilter extends Logging {

  private val preFilters: java.util.List[LinkisPreFilter] =
    new java.util.ArrayList[LinkisPreFilter]()

  def addFilter(linkisPreFilter: LinkisPreFilter): Unit = {
    logger.info(s"Add a filter with the following name: ${linkisPreFilter.name}")
    preFilters.add(linkisPreFilter)
  }

  def getLinkisPreFilters: java.util.List[LinkisPreFilter] = preFilters

}
