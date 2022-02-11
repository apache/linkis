package org.apache.linkis.gateway.authentication.service

import org.apache.linkis.gateway.authentication.bo.Token
import org.apache.linkis.gateway.authentication.bo.{Token, User}


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
