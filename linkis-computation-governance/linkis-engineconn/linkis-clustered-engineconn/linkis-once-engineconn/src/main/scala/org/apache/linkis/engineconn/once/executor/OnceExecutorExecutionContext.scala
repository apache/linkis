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

package org.apache.linkis.engineconn.once.executor

import org.apache.linkis.common.io.{FsPath, MetaData, Record}
import org.apache.linkis.common.io.resultset.{ResultSet, ResultSetWriter}
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.executor.ExecutorExecutionContext
import org.apache.linkis.governance.common.entity.job.OnceExecutorContent
import org.apache.linkis.storage.resultset.{ResultSetFactory, ResultSetWriterFactory}

class OnceExecutorExecutionContext(
    engineCreationContext: EngineCreationContext,
    onceExecutorContent: OnceExecutorContent
) extends ExecutorExecutionContext {

  private val resultSetFactory = ResultSetFactory.getInstance

  def getEngineCreationContext: EngineCreationContext = engineCreationContext

  def getOnceExecutorContent: OnceExecutorContent = onceExecutorContent

  override protected def getResultSetByType(
      resultSetType: String
  ): ResultSet[_ <: MetaData, _ <: Record] =
    resultSetFactory.getResultSetByType(resultSetType)

  override protected def getDefaultResultSetByType: String = resultSetFactory.getResultSetType()(0)

  override protected def newResultSetWriter(
      resultSet: ResultSet[_ <: MetaData, _ <: Record],
      resultSetPath: FsPath,
      alias: String
  ): org.apache.linkis.common.io.resultset.ResultSetWriter[_ <: MetaData, _ <: Record] =
    ResultSetWriterFactory.getResultSetWriter(
      resultSet,
      0,
      resultSetPath,
      engineCreationContext.getUser
    ) // OnceExecutor doesn't need to cache resultSet.

}
