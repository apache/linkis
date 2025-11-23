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

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{RSAUtils, Utils}
import org.apache.linkis.gateway.authentication.bo.{Token, User}
import org.apache.linkis.gateway.authentication.bo.impl.TokenImpl
import org.apache.linkis.gateway.authentication.conf.TokenConfiguration
import org.apache.linkis.gateway.authentication.dao.TokenDao
import org.apache.linkis.gateway.authentication.entity.TokenEntity
import org.apache.linkis.gateway.authentication.errorcode.LinkisGwAuthenticationErrorCodeSummary
import org.apache.linkis.gateway.authentication.errorcode.LinkisGwAuthenticationErrorCodeSummary._
import org.apache.linkis.gateway.authentication.exception.{
  TokenAuthException,
  TokenNotExistException
}
import org.apache.linkis.server.toScalaBuffer

import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang3.StringUtils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.text.MessageFormat
import java.util.concurrent.{ExecutionException, TimeUnit}

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import com.google.common.util.concurrent.UncheckedExecutionException

@Service
class CachedTokenService extends TokenService {

  @Autowired
  private var tokenDao: TokenDao = _

  private val tokenCache: LoadingCache[String, Token] = CacheBuilder.newBuilder
    .maximumSize(TokenConfiguration.TOKEN_CACHE_MAX_SIZE)
    .refreshAfterWrite(TokenConfiguration.TOKEN_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
    .build(new CacheLoader[String, Token]() {

      override def load(tokenName: String): Token = {
        val tokenEntity: TokenEntity = if (Configuration.LINKIS_RSA_TOKEN_SWITCH) {
          // 开关打开情况下，对token进行判断
          if (tokenName.startsWith(RSAUtils.PREFIX)) {
            // 传的是密文，直接查询tokenSign（密文保存在这里）
            tokenDao.selectTokenBySign(tokenName)
          } else {
            // 传入明文，首次执行模糊查询（兼容明文token未被加密，TokenSign为空，导致查询token异常）
            val tokenList = tokenDao.selectTokenByNameWithLike(tokenName)
            var token: TokenEntity = null
            if (CollectionUtils.isNotEmpty(tokenList)) {
              tokenList.foreach { tokenTmp =>
                if (
                    tokenTmp != null && StringUtils.isBlank(tokenTmp.getTokenSign) && tokenName
                      .equals(tokenTmp.getTokenName)
                ) {
                  token = tokenTmp
                }
              }
            }
            if (null == token) {
              // 兼容token被加密后，传入明文场景，需要执行截取规则后，查询tokenName
              token = tokenDao.selectTokenByName(RSAUtils.tokenSubRule(tokenName))
              if (token != null) {
                val realToken = RSAUtils.dncryptWithLinkisPublicKey(token.getTokenSign)
                if (!tokenName.equals(realToken)) {
                  throw new TokenNotExistException(
                    INVALID_TOKEN.getErrorCode,
                    INVALID_TOKEN.getErrorDesc
                  )
                }
              } else {
                throw new TokenNotExistException(
                  INVALID_TOKEN.getErrorCode,
                  INVALID_TOKEN.getErrorDesc
                )
              }
            }
            token
          }
        } else {
          // 开关没有打开情况下，旧数据没有加密，维持明文查询tokenName
          tokenDao.selectTokenByName(tokenName)
        }
        if (tokenEntity != null) {
          new TokenImpl().convertFrom(tokenEntity)
        } else {
          throw new TokenNotExistException(INVALID_TOKEN.getErrorCode, INVALID_TOKEN.getErrorDesc)
        }
      }

    });

  //  def setTokenDao(tokenDao: TokenDao): Unit = {
  //    this.tokenDao = tokenDao
  //  }

  /*
    TODO begin
   */
  override def addNewToken(token: Token): Boolean = {
    false
  }

  override def removeToken(tokenName: String): Boolean = {
    false
  }

  override def updateToken(token: Token): Boolean = {
    false
  }

  override def addUserForToken(tokenName: String, user: User): Boolean = {
    false
  }

  override def addHostForToken(tokenName: String, ip: String): Boolean = {
    false
  }

  override def addHostAndUserForToken(tokenName: String, user: User, ip: String): Boolean = {
    false
  }

  override def removeUserForToken(tokenName: String, user: User): Boolean = {
    false
  }

  override def removeHostForToken(tokenName: String, ip: String): Boolean = {
    false
  }

  /*
    TODO end
   */

  private def loadTokenFromCache(tokenName: String): Token = {
    if (tokenName == null) {
      throw new TokenAuthException(
        TOKEN_IS_NULL.getErrorCode,
        MessageFormat.format(TOKEN_IS_NULL.getErrorDesc, tokenName)
      )
    }
    Utils.tryCatch(tokenCache.get(tokenName))(t =>
      t match {
        case x: ExecutionException =>
          x.getCause match {
            case e: TokenNotExistException =>
              throwTokenAuthException(NOT_EXIST_DB, tokenName, e)
            case e =>
              throwTokenAuthException(FAILED_TO_LOAD_TOKEN, tokenName, e)
          }
        case e: UncheckedExecutionException =>
          throwTokenAuthException(FAILED_TO_BAD_SQLGRAMMAR, tokenName, e)
        case e =>
          throwTokenAuthException(FAILED_TO_LOAD_TOKEN, tokenName, e)
      }
    )
  }

  private def throwTokenAuthException(
      gwAuthenticationErrorCodeSummary: LinkisGwAuthenticationErrorCodeSummary,
      tokenName: String,
      e: Throwable
  ) = {
    val exception = new TokenAuthException(
      gwAuthenticationErrorCodeSummary.getErrorCode,
      MessageFormat.format(gwAuthenticationErrorCodeSummary.getErrorDesc, tokenName, e.getMessage)
    )
    exception.initCause(e)
    throw exception
  }

  private def isTokenAcceptableWithUser(token: Token, userName: String): Boolean = {
    token != null && !token.isStale() && token.isUserLegal(userName)
  }

  private def isTokenValid(token: Token): Boolean = {
    token != null && !token.isStale()
  }

  private def isTokenAcceptableWithHost(token: Token, host: String): Boolean = {
    token != null && !token.isStale() && token.isHostLegal(host)
  }

  override def isTokenValid(tokenName: String): Boolean = {
    isTokenValid(loadTokenFromCache(tokenName))
  }

  override def isTokenAcceptableWithUser(tokenName: String, userName: String): Boolean = {
    isTokenAcceptableWithUser(loadTokenFromCache(tokenName), userName)
  }

  override def isTokenAcceptableWithHost(tokenName: String, host: String): Boolean = {
    isTokenAcceptableWithHost(loadTokenFromCache(tokenName), host)
  }

  override def doAuth(tokenName: String, userName: String, host: String): Boolean = {
    val tmpToken: Token = loadTokenFromCache(tokenName)
    var ok: Boolean = true
    // token expired
    if (!isTokenValid(tmpToken)) {
      ok = false
      throw new TokenAuthException(
        TOKEN_IS_EXPIRED.getErrorCode,
        MessageFormat.format(TOKEN_IS_EXPIRED.getErrorDesc, tokenName)
      )
    }
    if (!isTokenAcceptableWithUser(tmpToken, userName)) {
      ok = false
      throw new TokenAuthException(
        ILLEGAL_TOKENUSER.getErrorCode,
        MessageFormat.format(ILLEGAL_TOKENUSER.getErrorDesc, userName)
      )
    }
    if (!isTokenAcceptableWithHost(tmpToken, host)) {
      ok = false
      throw new TokenAuthException(
        ILLEGAL_HOST.getErrorCode,
        MessageFormat.format(ILLEGAL_HOST.getErrorDesc, host)
      )
    }
    ok
  }

}
