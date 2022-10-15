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

package org.apache.linkis.engineplugin.elasticsearch.factory

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.computation.executor.creation.ComputationExecutorFactory
import org.apache.linkis.engineconn.computation.executor.execute.ComputationExecutor
import org.apache.linkis.engineconn.core.executor.ExecutorManager
import org.apache.linkis.engineplugin.elasticsearch.conf.ElasticSearchConfiguration
import org.apache.linkis.engineplugin.elasticsearch.executor.ElasticSearchEngineConnExecutor
import org.apache.linkis.governance.common.paser.SQLCodeParser
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType

class ElasticSearchSqlExecutorFactory extends ComputationExecutorFactory {

  override protected def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn,
      labels: Array[Label[_]]
  ): ComputationExecutor = {
    val executor = new ElasticSearchEngineConnExecutor(
      ElasticSearchConfiguration.ENGINE_DEFAULT_LIMIT.getValue,
      id,
      RunType.ES_SQL.toString
    )
    executor.setCodeParser(new SQLCodeParser)
    executor
  }

  override protected def getRunType: RunType = RunType.ES_SQL

}
