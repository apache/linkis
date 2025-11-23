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

package org.apache.linkis.entrance.persistence

import org.apache.linkis.entrance.EntranceContext
import org.apache.linkis.scheduler.listener.{JobListener, JobRetryListener, ProgressListener}

abstract class PersistenceManager
    extends JobListener
    with ResultSetListener
    with ProgressListener
    with JobRetryListener {

  def getEntranceContext: EntranceContext

  def setEntranceContext(entranceContext: EntranceContext): Unit

  def createPersistenceEngine(): PersistenceEngine

  def createResultSetEngine(): ResultSetEngine

  // TODO Need to support HA(需要支持HA)

}
