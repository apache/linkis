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

package org.apache.linkis.gateway.authentication.bo.impl

import org.apache.linkis.gateway.authentication.bo.{Token, User}
import org.apache.linkis.gateway.authentication.bo.Token
import org.apache.linkis.gateway.authentication.conf.TokenConfiguration
import org.apache.linkis.gateway.authentication.entity.TokenEntity

import org.apache.commons.lang3.StringUtils

import java.util.Date

class TokenImpl extends Token {

  private var id: String = _
  private var tokenName: String = _
  private var businessOwner: String = _
  private var createTime: Date = _
  private var updateTime: Date = _
  private var elapseDay: Long = 0L
  private var updateBy: String = _

  private var legalUsersName: Set[String] = _
  private var legalHosts: Set[String] = _

  def convertFrom(tokenEntity: TokenEntity): TokenImpl = {
    id = tokenEntity.getId
    tokenName = tokenEntity.getTokenName
    if (StringUtils.isNotBlank(tokenEntity.getLegalUsersStr)) {
      legalUsersName = getLegalUsersInternal(tokenEntity.getLegalUsersStr)
    }
    if (StringUtils.isNotBlank(tokenEntity.getLegalHostsStr)) {
      legalHosts = getLegalHosts(tokenEntity.getLegalHostsStr)
    }
    businessOwner = tokenEntity.getBusinessOwner
    createTime = tokenEntity.getCreateTime
    updateTime = tokenEntity.getUpdateTime
    elapseDay = tokenEntity.getElapseDay
    updateBy = tokenEntity.getUpdateBy
    this
  }

  private def getLegalUsersInternal(legalUsersStr: String): Set[String] = {
    if (StringUtils.isBlank(legalUsersStr)) {
      null
    } else {
      legalUsersStr.split(',').toSet
    }
  }

  private def getLegalHosts(legalHostsStr: String): Set[String] = {
    if (StringUtils.isBlank(legalHostsStr)) {
      null
    } else {
      legalHostsStr.split(',').toSet
    }
  }

  override def getTokenName(): String = {
    tokenName
  }

  /**
   * 1 token corresponds to multiple users
   */
  override def getLegalUsers(): Set[User] = {
    legalUsersName.map(u => new UserImpl(u))
  }

  override def isUserLegal(user: String): Boolean = {
    allowAllUsers || (legalUsersName != null && legalUsersName.contains(user))
  }

  private def allowAllUsers(): Boolean = {
    legalUsersName != null && legalUsersName.contains(TokenConfiguration.TOKEN_WILD_CHAR)
  }

  /**
   * 1 token corresponds to multiple Hosts
   */
  override def getLegalHosts(): Set[String] = {
    legalHosts
  }

  override def isHostLegal(host: String): Boolean = {
    allowAllHosts || (legalHosts != null && legalHosts.contains(host))
  }

  private def allowAllHosts(): Boolean = {
    legalHosts != null && legalHosts.contains(TokenConfiguration.TOKEN_WILD_CHAR)
  }

  override def getCreateTime(): Date = {
    createTime
  }

  override def getUpdateTime(): Date = {
    updateTime
  }

  override def setUpdateTime(updateTime: Date): Unit = {
    this.updateTime = updateTime
  }

  override def getElapseDay(): Long = {
    elapseDay
  }

  /**
   * Unit: second
   */
  override def setElapseDay(elapseDay: Long): Unit = {
    this.elapseDay = elapseDay
  }

  override def getBusinessOwner(): String = {
    businessOwner
  }

  /**
   * 1 token corresponds to 1 business owner
   */
  override def setBusinessOwner(businessOwner: String): Unit = {
    this.businessOwner = businessOwner
  }

  override def getUpdateBy(): String = {
    updateBy
  }

  override def setUpdateBy(updateBy: String): Unit = {
    this.updateBy = updateBy
  }

  /**
   * decided by createdTime and elapseTime, if elapseTime = -1 then will not stale forever
   */
  override def isStale(): Boolean = {
    elapseDay != -1 && System
      .currentTimeMillis() > (createTime.getTime + elapseDay * 24 * 3600 * 1000)
  }

}
