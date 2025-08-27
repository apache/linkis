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

package org.apache.linkis.gateway.authentication.dao;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.gateway.authentication.entity.TokenEntity;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class TokenDaoTest extends BaseDaoTest {

  private static final Logger logger = LoggerFactory.getLogger(BaseDaoTest.class);

  private static String TokenName =
      CommonVars.apply("wds.linkis.bml.auth.token.value", "LINKIS-AUTH").getValue();

  @Autowired TokenDao tokenDao;

  @Test
  void testSelectTokenByName() {
    TokenEntity result = tokenDao.selectTokenByName(TokenName);
    assertEquals(result.getTokenName(), TokenName);
  }

  @Test
  void testGetAllTokens() {
    List<TokenEntity> result = tokenDao.getAllTokens();
    assertNotEquals(result.size(), 0);
  }
}
