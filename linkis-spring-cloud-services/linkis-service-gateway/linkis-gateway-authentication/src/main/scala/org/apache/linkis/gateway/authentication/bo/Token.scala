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

package org.apache.linkis.gateway.authentication.bo

import java.util.Date

trait Token {

  def getTokenName(): String

  /**
   * 1 token corresponds to multiple users
   */
  def getLegalUsers(): Set[User]

  def isUserLegal(user: String): Boolean

  /**
   * 1 token corresponds to multiple Hosts
   */
  def getLegalHosts(): Set[String]

  def isHostLegal(host: String): Boolean

  def getCreateTime(): Date

  def setUpdateTime(updateTime: Date)

  def getUpdateTime(): Date

  /**
   * Unit: second
   */
  def setElapseDay(elapseDay: Long)

  def getElapseDay(): Long

  /**
   * 1 token corresponds to 1 business owner
   */
  def setBusinessOwner(businessOwner: String)

  def getBusinessOwner(): String

  def setUpdateBy(updateBy: String)

  def getUpdateBy(): String

  /**
   * decided by createdTime and elapseTime, if elapseTime = -1 then will not stale forever
   */
  def isStale(): Boolean
}
