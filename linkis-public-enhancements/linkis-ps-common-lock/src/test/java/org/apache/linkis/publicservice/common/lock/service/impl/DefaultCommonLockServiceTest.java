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

package org.apache.linkis.publicservice.common.lock.service.impl;

import org.apache.linkis.publicservice.common.lock.dao.CommonLockMapper;
import org.apache.linkis.publicservice.common.lock.entity.CommonLock;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DefaultCommonLockServiceTest {

  @InjectMocks private DefaultCommonLockService commonLockService;

  @Mock private CommonLockMapper commonLockMapper;

  @Test
  @DisplayName("lockTest")
  public void lockTest() {

    CommonLock commonLock = new CommonLock();
    commonLock.setLockObject("hadoops");
    Long timeOut = 10000L;
    Boolean lock = commonLockService.lock(commonLock, timeOut);

    Assertions.assertTrue(lock.booleanValue());
  }

  @Test
  @DisplayName("getAllTest")
  public void getAllTest() {

    List<CommonLock> locks = commonLockService.getAll();
    Assertions.assertTrue(locks.size() == 0);
  }
}
