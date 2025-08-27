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

package org.apache.linkis.manager.dao;

import org.apache.linkis.manager.common.entity.persistence.PersistenceLock;

import org.apache.ibatis.annotations.*;

import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Mapper
public interface LockManagerMapper {

  @Retryable(
      value = {CannotGetJdbcConnectionException.class},
      maxAttempts = 6,
      backoff = @Backoff(delay = 10000))
  @Transactional(rollbackFor = Exception.class)
  int lock(PersistenceLock persistenceLock);

  @Transactional(rollbackFor = Exception.class)
  void unlock(@Param("id") Integer id);

  Integer getMinimumOrder(@Param("lockObject") String lockObject, @Param("id") Integer id);

  List<PersistenceLock> getLockersByLockObject(@Param("lock_object") String lock_object);

  List<PersistenceLock> getAll();

  List<PersistenceLock> getTimeOutLocks(@Param("endDate") Date endDate);
}
