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

package org.apache.linkis.gateway.authentication.service;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.gateway.authentication.Scan;
import org.apache.linkis.gateway.authentication.WebApplicationServer;
import org.apache.linkis.gateway.authentication.exception.TokenAuthException;

import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {WebApplicationServer.class, Scan.class})
public class CachedTokenServiceTest {
  private static final Logger logger = LoggerFactory.getLogger(CachedTokenServiceTest.class);

  private static String TokenName =
      CommonVars.apply("wds.linkis.bml.auth.token.value", Configuration.LINKIS_TOKEN().getValue())
          .getValue();

  @Autowired CachedTokenService tokenService;

  @BeforeAll
  static void before() {
    if (StringUtils.isBlank(TokenName)) {
      TokenName = "LINKIS-UNAVAILABLE-TOKE";
    }
  }

  @Test
  void testIsTokenValid() {
    boolean isOk = tokenService.isTokenValid(TokenName);
    assertTrue(isOk);
  }

  @Test
  void testIsTokenAcceptableWithUser() {
    boolean isOk = tokenService.isTokenAcceptableWithUser(TokenName, "test");
    assertTrue(isOk);
    isOk = tokenService.isTokenAcceptableWithUser(TokenName, "test1");
    assertFalse(isOk);
  }

  @Test
  void testIsTokenAcceptableWithHost() {
    boolean isOk = tokenService.isTokenAcceptableWithHost(TokenName, "127.0.0.1");
    assertTrue(isOk);
    isOk = tokenService.isTokenAcceptableWithHost(TokenName, "10.10.10.10");
    assertFalse(isOk);
  }

  @Test
  void testDoAuth() {
    boolean isOk = tokenService.doAuth(TokenName, "test", "127.0.0.1");
    assertTrue(isOk);

    Exception exception =
        assertThrows(
            TokenAuthException.class, () -> tokenService.doAuth(TokenName, "test1", "127.0.0.1"));
    logger.info("assertThrows：{}", exception.getMessage());

    exception =
        assertThrows(
            TokenAuthException.class, () -> tokenService.doAuth(TokenName, "test", "10.10.10.10"));
    logger.info("assertThrows：{}", exception.getMessage());
  }
}
