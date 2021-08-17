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
package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.factory

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import com.webank.wedatasphere.linkis.manager.engineplugin.common.creation.{ExecutorFactory, MultiExecutorEngineConnFactory}
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineType.EngineType

class ElasticSearchEngineConnFactory extends MultiExecutorEngineConnFactory with Logging {
  private val executorFactoryArray =   Array[ExecutorFactory](new ElasticSearchJsonExecutorFactory, new ElasticSearchSqlExecutorFactory)

  override def getExecutorFactories: Array[ExecutorFactory] = {
    executorFactoryArray
  }

  override protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory] =
    classOf[ElasticSearchJsonExecutorFactory]

  override protected def getEngineConnType: EngineType = EngineType.ELASTICSEARCH

  override protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any = null
}
