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

package org.apache.linkis.gateway.authentication.service

import org.apache.linkis.gateway.authentication.bo.{Token, User}
import org.apache.linkis.gateway.authentication.bo.Token

trait TokenService {

  def addNewToken(token: Token): Boolean

  def removeToken(tokenName: String): Boolean

  def updateToken(token: Token): Boolean

  def addUserForToken(tokenName: String, user: User): Boolean

  def addHostForToken(tokenName: String, ip: String): Boolean

  def addHostAndUserForToken(tokenName: String, user: User, ip: String): Boolean

  def removeUserForToken(tokenName: String, user: User): Boolean

  def removeHostForToken(tokenName: String, ip: String): Boolean

  def isTokenValid(tokenName: String): Boolean

  def isTokenAcceptableWithUser(tokenName: String, userName: String): Boolean

  def isTokenAcceptableWithHost(tokenName: String, host: String): Boolean

  def doAuth(tokenName: String, userName: String, host: String): Boolean
}
