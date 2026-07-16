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

package org.apache.linkis.server.security

import org.junit.jupiter.api.{Assertions, DisplayName, Test}
import org.mockito.Mockito.{mock, when}

import javax.servlet.http.{Cookie, HttpServletRequest}

class SecurityFilterTest {

  private val IGNORE_COOKIE_NAME = SecurityFilter.ALLOW_ACCESS_WITHOUT_TIMEOUT

  private def mockRequest(remoteAddr: String, cookies: Array[Cookie] = null,
                          xForwardedFor: String = null): HttpServletRequest = {
    val req = mock(classOf[HttpServletRequest])
    when(req.getRemoteAddr).thenReturn(remoteAddr)
    when(req.getCookies).thenReturn(cookies)
    if (xForwardedFor != null) {
      when(req.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor)
    } else {
      when(req.getHeader("X-Forwarded-For")).thenReturn(null)
    }
    req
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsFalseWhenCookieAbsent")
  def isRequestIgnoreTimeoutNoCookieTest(): Unit = {
    val req = mockRequest("127.0.0.1", cookies = null)
    Assertions.assertFalse(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsTrueWhenCookieFromLoopback")
  def isRequestIgnoreTimeoutFromLoopbackTest(): Unit = {
    val req = mockRequest("127.0.0.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")))
    Assertions.assertTrue(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsFalseWhenCookieFromExternalIP")
  def isRequestIgnoreTimeoutFromExternalIPTest(): Unit = {
    // 203.0.113.0/24 is TEST-NET-3, not in default RFC1918 trusted sources
    val req = mockRequest("203.0.113.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")))
    Assertions.assertFalse(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsTrueFromPrivate10x")
  def isRequestIgnoreTimeoutFromPrivate10xTest(): Unit = {
    val req = mockRequest("10.255.255.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")))
    Assertions.assertTrue(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsTrueFromPrivate172x")
  def isRequestIgnoreTimeoutFromPrivate172xTest(): Unit = {
    val req = mockRequest("172.31.0.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")))
    Assertions.assertTrue(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_returnsFalseWhenCookieValueIsFalse")
  def isRequestIgnoreTimeoutCookieFalseTest(): Unit = {
    val req = mockRequest("127.0.0.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "false")))
    Assertions.assertFalse(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_honorsXForwardedForFromTrustedUpstream")
  def isRequestIgnoreTimeoutWithXForwardedForTest(): Unit = {
    val req = mockRequest("127.0.0.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")),
      xForwardedFor = "10.0.0.5, 203.0.113.1")
    Assertions.assertTrue(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("isRequestIgnoreTimeout_ignoresXForwardedForFromUntrustedUpstream")
  def isRequestIgnoreTimeoutXForwardedForUntrustedTest(): Unit = {
    val req = mockRequest("203.0.113.1",
      cookies = Array(new Cookie(IGNORE_COOKIE_NAME, "true")),
      xForwardedFor = "10.0.0.5")
    Assertions.assertFalse(SecurityFilter.isRequestIgnoreTimeout(req))
  }

  @Test
  @DisplayName("getLoginUserThrowsExceptionWhenTimeout_rejectsHeaderFallbackFromExternalIP")
  def getLoginUserHeaderFallbackFromExternalIPTest(): Unit = {
    val req = mockRequest("203.0.113.1", cookies = null)
    val result = SecurityFilter.getLoginUserThrowsExceptionWhenTimeout(req)
    Assertions.assertTrue(result.isEmpty)
  }

  @Test
  @DisplayName("ignoreTimeoutSignal_createsCookieWithCorrectDefaults")
  def ignoreTimeoutSignalDefaultsTest(): Unit = {
    val cookie = SecurityFilter.ignoreTimeoutSignal()
    Assertions.assertEquals(IGNORE_COOKIE_NAME, cookie.getName)
    Assertions.assertEquals("true", cookie.getValue)
    Assertions.assertEquals(-1, cookie.getMaxAge)
    Assertions.assertEquals("/", cookie.getPath)
  }
}
