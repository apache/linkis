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

package org.apache.linkis.engineplugin.spark.factory

import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.EngineConn
import org.apache.linkis.engineconn.once.executor.OnceExecutor
import org.apache.linkis.engineconn.once.executor.creation.OnceExecutorFactory
import org.apache.linkis.engineplugin.spark.context.SparkEngineConnContext
import org.apache.linkis.engineplugin.spark.executor.{
  SparkOnKubernetesSubmitOnceExecutor,
  SparkSubmitOnceExecutor
}
import org.apache.linkis.manager.common.conf.RMConfiguration.DEFAULT_KUBERNETES_TYPE
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.cluster.ClusterLabel
import org.apache.linkis.manager.label.entity.engine.RunType
import org.apache.linkis.manager.label.entity.engine.RunType.RunType
import org.apache.linkis.manager.label.utils.LabelUtil

class SparkOnceExecutorFactory extends OnceExecutorFactory {

  override protected def newExecutor(
      id: Int,
      engineCreationContext: EngineCreationContext,
      engineConn: EngineConn,
      labels: Array[Label[_]]
  ): OnceExecutor = {
    val clusterLabel = LabelUtil.getLabelFromArray[ClusterLabel](labels)
    engineConn.getEngineConnSession match {
      case context: SparkEngineConnContext =>
        if (
            null != clusterLabel && clusterLabel.getClusterType.equalsIgnoreCase(
              DEFAULT_KUBERNETES_TYPE.getValue
            )
        ) {
          new SparkOnKubernetesSubmitOnceExecutor(id, context)
        } else {
          new SparkSubmitOnceExecutor(id, context)
        }
    }
  }

  override protected def getSupportRunTypes: Array[String] =
    Array(RunType.JAR.toString, RunType.PYSPARK.toString)

  override protected def getRunType: RunType = RunType.JAR
}
