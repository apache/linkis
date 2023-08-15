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

package org.apache.linkis.engineconnplugin.flink.operator.clientmanager

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconnplugin.flink.config.FlinkEnvConfiguration
import org.apache.linkis.engineconnplugin.flink.executor.FlinkManagerConcurrentExecutor
import org.apache.linkis.engineconnplugin.flink.factory.FlinkManagerExecutorFactory
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil
import org.apache.linkis.engineconnplugin.flink.util.YarnUtil.logAndException

import org.apache.flink.client.program.rest.RestClusterClient
import org.apache.flink.configuration.Configuration
import org.apache.hadoop.yarn.api.records.{ApplicationId, FinalApplicationStatus}

import java.util.concurrent.TimeUnit

import com.google.common.cache.{
  CacheBuilder,
  CacheLoader,
  LoadingCache,
  RemovalListener,
  RemovalNotification
}

object FlinkRestClientManager extends Logging {

  private lazy val restclientCache
      : LoadingCache[String, RestClusterClient[ApplicationId]] = CacheBuilder
    .newBuilder()
    .maximumSize(FlinkEnvConfiguration.FLINK_MANAGER_CLIENT_MAX_NUM.getValue)
    .expireAfterAccess(
      FlinkEnvConfiguration.FLINK_MANAGER_CLIENT_EXPIRE_MILLS.getValue,
      TimeUnit.MILLISECONDS
    )
    .weakKeys()
    .removalListener(new RemovalListener[String, RestClusterClient[ApplicationId]]() {

      override def onRemoval(
          notification: RemovalNotification[String, RestClusterClient[ApplicationId]]
      ): Unit = {
        logger.info(s"RestClusterClient of AppId : ${notification.getKey} was removed.")
      }

    })
    .build(new CacheLoader[String, RestClusterClient[ApplicationId]]() {

      override def load(appIdStr: String): RestClusterClient[ApplicationId] = {

        val appId: ApplicationId = YarnUtil.retrieveApplicationId(appIdStr)

        val yarnClient = YarnUtil.getYarnClient()
        val appReport = yarnClient.getApplicationReport(appId)

        if (appReport.getFinalApplicationStatus != FinalApplicationStatus.UNDEFINED) {
          // Flink cluster is not running anymore
          val msg =
            s"The application ${appIdStr} doesn't run anymore. It has previously completed with final status: ${appReport.getFinalApplicationStatus.toString}"
          throw logAndException(msg)
        }

        val executor = FlinkManagerExecutorFactory.getDefaultExecutor()
        val tmpFlinkConf: Configuration = executor match {
          case flinkManagerExecutor: FlinkManagerConcurrentExecutor =>
            flinkManagerExecutor.getFlinkContext().getEnvironmentContext.getFlinkConfig.clone()
          case _ =>
            val msg = s"Invalid FlinkManagerConcurrentExecutor : ${executor}"
            throw logAndException(msg)
        }
        YarnUtil.setClusterEntrypointInfoToConfig(tmpFlinkConf, appReport)
        new RestClusterClient[ApplicationId](tmpFlinkConf, appReport.getApplicationId)
      }

    })

  def getFlinkRestClient(appIdStr: String): RestClusterClient[ApplicationId] =
    restclientCache.get(appIdStr)

  def setFlinkRestClient(appIdStr: String, client: RestClusterClient[ApplicationId]): Unit =
    restclientCache.put(appIdStr, client)

}
