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

package org.apache.linkis.gateway.security.oauth

import org.apache.linkis.common.exception.LinkisCommonErrorException
import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.gateway.config.GatewayConfiguration
import org.apache.linkis.gateway.config.GatewayConfiguration._
import org.apache.linkis.gateway.http.GatewayContext
import org.apache.linkis.gateway.security.{GatewaySSOUtils, SecurityFilter}
import org.apache.linkis.server.Message
import org.apache.linkis.server.conf.ServerConfiguration

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils

import java.io.IOException
import java.net.{HttpURLConnection, URL}

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object OAuth2Authentication extends Logging {

  private val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  def isOAuth2Request(gatewayContext: GatewayContext): Boolean = {
    val path = getMethod(gatewayContext)
    path == "oauth_login" || path == "oauth_redirect"
  }

  def OAuth2Entry(gatewayContext: GatewayContext, login: Boolean = false): Boolean = {
    val path = getMethod(gatewayContext)
    if (path == "oauth_redirect") {
      OAuth2Redirect(gatewayContext)
    } else if (path == "oauth_redirect") {
      OAuth2Auth(gatewayContext, login)
    } else {
      val message =
        Message.noLogin(s"未知 OAuth 请求") << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      false
    }
  }

  private def getMethod(gatewayContext: GatewayContext) = {
    var userURI = ServerConfiguration.BDP_SERVER_USER_URI.getValue
    if (!userURI.endsWith("/")) userURI += "/"
    val path = gatewayContext.getRequest.getRequestURI.replace(userURI, "")
    path
  }

  def OAuth2Redirect(gatewayContext: GatewayContext): Boolean = {
    if (!ENABLE_OAUTH_AUTHENTICATION.getValue) {
      val message =
        Message.noLogin(
          s"Gateway 未启用 OAuth 认证，请采用其他认证方式!"
        ) << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      return false
    }
    val message =
      Message.ok("创建链接成功！").data("redirectUrl", generateAuthenticationUrl())
    SecurityFilter.filterResponse(gatewayContext, message)
    true
  }

  /**
   * 生成OAuth认证的URL
   *
   * @note
   *   认证完成回调链接需要在认证服务器上进行配置
   * @return
   */
  private def generateAuthenticationUrl(): String = {
    var oauthServerUrl =
      s"${OAUTH_AUTHENTICATION_URL.getValue}?client_id=${OAUTH_CLIENT_ID.getValue}&response_type=code"
    if (StringUtils.isNotBlank(OAUTH_SCOPE.getValue)) {
      oauthServerUrl += s"&scope=${OAUTH_SCOPE.getValue}"
    }
    oauthServerUrl
  }

  def OAuth2Auth(gatewayContext: GatewayContext, login: Boolean = false): Boolean = {
    if (!ENABLE_OAUTH_AUTHENTICATION.getValue) {
      val message =
        Message.noLogin(
          s"Gateway 未启用 OAuth 认证，请采用其他认证方式!"
        ) << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      return false
    }

    val code = extractCode(gatewayContext)
    val host = gatewayContext.getRequest.getRequestRealIpAddr()

    if (StringUtils.isBlank(code)) {
      val message =
        Message.noLogin(s"请在回调查询参数中返回code，以便完成OAuth认证！") << gatewayContext.getRequest.getRequestURI
      SecurityFilter.filterResponse(gatewayContext, message)
      return false
    }

    var authMsg: Message =
      Message.noLogin(s"无效的访问令牌 $code，无法完成 OAuth 认证!") << gatewayContext.getRequest.getRequestURI

    val accessToken = Utils.tryCatch(exchangeAccessToken(code, host))(t => {
      authMsg = Message.noLogin(
        s"OAuth exchange failed, code: $code, reason: ${t.getMessage}"
      ) << gatewayContext.getRequest.getRequestURI
      null
    })

    if (StringUtils.isNotBlank(accessToken)) {
      val username = validateAccessToken(accessToken, host)
      logger.info(
        s"OAuth authentication succeed, uri: ${gatewayContext.getRequest.getRequestURI}, accessToken: $accessToken, username: $username."
      )

      if (login) {
        GatewaySSOUtils.setLoginUser(gatewayContext, username)
        val msg =
          Message
            .ok("login successful(登录成功)！")
            .data("userName", username)
            .data("enableWatermark", GatewayConfiguration.ENABLE_WATER_MARK.getValue)
            .data("isAdmin", false)
        SecurityFilter.filterResponse(gatewayContext, msg)
        return true
      }

      GatewaySSOUtils.setLoginUser(gatewayContext.getRequest, username)
      true
    } else {
      logger.info(
        s"OAuth exchange fail, uri: ${gatewayContext.getRequest.getRequestURI}, code: $code, host: $host."
      )
      SecurityFilter.filterResponse(gatewayContext, authMsg)
      false
    }
  }

  private def extractCode(gatewayContext: GatewayContext): String = {
    Utils.tryCatch(gatewayContext.getRequest.getQueryParams.get("code")(0))(_ => null)
  }

  /**
   * 验证访问码的有效性并获取访问令牌
   *
   * @param code
   *   访问码
   * @param host
   *   客户端主机
   * @return
   *   访问令牌
   */
  private def exchangeAccessToken(code: String, host: String): String = {
    val exchangeUrl = OAUTH_EXCHANGE_URL.getValue

    if (StringUtils.isBlank(exchangeUrl)) {
      logger.warn(s"OAuth exchange url is not set")
    }
    if (StringUtils.isBlank(code)) {
      logger.warn(s"OAuth exchange code is empty")
    }

    Utils.tryCatch({
      val response = HttpUtils.post(
        exchangeUrl,
        data = objectMapper.writeValueAsString(
          Map(
            "client_id" -> OAUTH_CLIENT_ID.getValue,
            "client_secret" -> OAUTH_CLIENT_SECRET.getValue,
            "code" -> code,
            "host" -> host
          )
        )
      )
      objectMapper.readValue(response, classOf[Map[String, String]]).get("access_token").orNull
    })(t => {
      logger.warn(s"OAuth exchange failed, url: $exchangeUrl, reason: ${t.getMessage}")
      null
    })
  }

  /**
   * 验证访问令牌的有效性并兑换用户名
   *
   * @param accessToken
   *   访问令牌
   * @param host
   *   客户端主机
   * @return
   *   用户名
   */
  private def validateAccessToken(accessToken: String, host: String): String = {
    val url = OAUTH_VALIDATE_URL.getValue

    if (StringUtils.isBlank(url)) {
      logger.warn(s"OAuth validate url is not set")
    }

    if (StringUtils.isBlank(accessToken)) {
      logger.warn(s"OAuth validate accessToken is empty")
    }

    Utils.tryCatch({
      val response = HttpUtils.get(url, headers = Map("Authorization" -> s"Bearer $accessToken"))
      objectMapper
        .readValue(response, classOf[Map[String, String]])
        .get(OAUTH_VALIDATE_FIELD.getValue)
        .orNull
    })(t => {
      logger.warn(s"OAuth validate failed, url: $url, reason: ${t.getMessage}")
      null
    })
  }

}

object HttpUtils extends Logging {

  def get(
      url: String,
      headers: Map[String, String] = Map.empty,
      params: Map[String, String] = Map.empty
  ): String = {
    Utils.tryCatch {
      val fullUrl = url + (if (params.nonEmpty) {
                             "?" + params.map { case (key, value) => s"$key=$value" }.mkString("&")
                           } else {
                             ""
                           })
      val connection = new URL(fullUrl).openConnection().asInstanceOf[HttpURLConnection]
      connection.setRequestMethod("GET")

      headers.foreach { case (key, value) =>
        connection.setRequestProperty(key, value)
      }

      if (!headers.contains("Accept")) {
        connection.setRequestProperty("Accept", "application/json")
      }

      val responseCode = connection.getResponseCode
      if (!(responseCode >= 200 && responseCode < 300)) {
        throw new IOException(s"HTTP GET request failed for URL: $url - $responseCode")
      }

      val inputStream = connection.getInputStream

      try {
        IOUtils.toString(inputStream, "UTF-8")
      } finally {
        inputStream.close()
        connection.disconnect()
      }
    } { t =>
      logger.warn(s"Failed to execute HTTP GET request to $url", t)
      throw new LinkisCommonErrorException(
        0,
        s"HTTP GET request failed for URL: $url, reason: ${t.getMessage}"
      )
    }
  }

  def post(url: String, data: String, headers: Map[String, String] = Map.empty): String = {
    Utils.tryCatch {
      val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]
      try {
        connection.setRequestMethod("POST")
        connection.setDoOutput(true)
        connection.setDoInput(true)

        headers.foreach { case (key, value) =>
          connection.setRequestProperty(key, value)
        }

        if (!headers.contains("Content-Type")) {
          connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        }

        if (!headers.contains("Accept")) {
          connection.setRequestProperty("Accept", "application/json")
        }

        if (data != null && data.nonEmpty) {
          val outputStream = connection.getOutputStream
          try {
            IOUtils.write(data, outputStream, "UTF-8")
          } finally {
            outputStream.close()
          }
        }

        val responseCode = connection.getResponseCode
        if (!(responseCode >= 200 && responseCode < 300)) {
          throw new IOException(s"HTTP POST request failed for URL: $url - $responseCode")
        }

        val inputStream = connection.getInputStream

        try {
          if (inputStream != null) {
            IOUtils.toString(inputStream, "UTF-8")
          } else {
            ""
          }
        } finally {
          if (inputStream != null) inputStream.close()
        }
      } finally {
        connection.disconnect()
      }
    } { t =>
      logger.warn(s"Failed to execute HTTP POST request to $url", t)
      throw new LinkisCommonErrorException(
        0,
        s"HTTP POST request failed for URL: $url, reason: ${t.getMessage}"
      )
    }
  }

}
