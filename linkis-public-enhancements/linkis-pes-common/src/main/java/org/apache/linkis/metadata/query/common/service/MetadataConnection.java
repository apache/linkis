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

package org.apache.linkis.metadata.query.common.service;

import java.util.concurrent.locks.ReentrantLock;

/** Connection for metadata */
public class MetadataConnection<C> {
  /** * Avoid complication when different threads calling connection API */
  private ReentrantLock lock = new ReentrantLock();

  private boolean isLock = true;

  private C connection;

  public MetadataConnection(C connection) {
    this.connection = connection;
  }

  public MetadataConnection(C connection, boolean isLock) {
    this.connection = connection;
    this.isLock = isLock;
  }

  public ReentrantLock getLock() {
    return lock;
  }

  public C getConnection() {
    return connection;
  }

  public boolean isLock() {
    return isLock;
  }

  public void setLock(boolean lock) {
    isLock = lock;
  }
}
