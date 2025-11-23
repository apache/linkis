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

package org.apache.linkis.datasourcemanager.core.dao;

import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceTypeDaoTest extends BaseDaoTest {
  private static final Logger logger = LoggerFactory.getLogger(DataSourceTypeDaoTest.class);

  @Autowired DataSourceTypeDao dataSourceTypeDao;

  @AfterAll
  @DisplayName("Each unit test method is executed once before execution")
  protected static void afterAll() throws Exception {}

  @Test
  void testGetAllTypes() {
    assertTrue(dataSourceTypeDao.getAllTypes().size() == 1);
  }

  @Test
  void testSelectOne() {
    assertNotNull(dataSourceTypeDao.selectOne(1l));
  }
}
