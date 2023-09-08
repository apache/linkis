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

package org.apache.linkis.engineplugin.spark.utils

import org.apache.linkis.common.utils.{JsonUtils, Logging}
import org.apache.linkis.engineconn.launch.EngineConnServer
import org.apache.linkis.engineplugin.spark.config.SparkConfiguration.SPARK_ONCE_YARN_RESTFUL_URL
import org.apache.linkis.protocol.engine.JobProgressInfo

import org.apache.commons.lang3.StringUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import java.util

object SparkJobProgressUtil extends Logging {

  def getProgress(applicationId: String, podIP: String = ""): Float = {
    if (StringUtils.isBlank(applicationId)) return 0f
    val sparkJobsResult =
      if (StringUtils.isBlank(podIP)) getSparkJobInfo(applicationId)
      else getKubernetesSparkJobInfo(applicationId, podIP)
    if (sparkJobsResult.isEmpty) return 0f
    val tuple = sparkJobsResult
      .filter(sparkJobResult => {
        val status = sparkJobResult.getOrDefault("status", "").asInstanceOf[String]
        "RUNNING".equals(status) || "SUCCEEDED".equals(status)
      })
      .map(result =>
        (
          result.get("numTasks").asInstanceOf[Integer],
          result.get("numCompletedTasks").asInstanceOf[Integer]
        )
      )
      .reduce((total, completed) => (total._1 + completed._1, total._2 + completed._2))
    tuple._2.toFloat / tuple._1
  }

  def getSparkJobProgressInfo(applicationId: String, podIP: String = ""): Array[JobProgressInfo] = {
    val sparkJobsResult =
      if (StringUtils.isBlank(podIP)) getSparkJobInfo(applicationId)
      else getKubernetesSparkJobInfo(applicationId, podIP)
    if (sparkJobsResult.isEmpty) {
      Array.empty
    } else {
      sparkJobsResult.map(sparkJobResult =>
        JobProgressInfo(
          getJobId(
            sparkJobResult.get("jobId").asInstanceOf[Integer],
            sparkJobResult.get("jobGroup").asInstanceOf[String]
          ),
          sparkJobResult.get("numTasks").asInstanceOf[Integer],
          sparkJobResult.get("numActiveTasks").asInstanceOf[Integer],
          sparkJobResult.get("numFailedTasks").asInstanceOf[Integer],
          sparkJobResult.get("numCompletedTasks").asInstanceOf[Integer]
        )
      )
    }
  }

  def getSparkJobInfo(applicationId: String): Array[java.util.Map[String, Object]] =
    if (StringUtils.isBlank(applicationId)) Array.empty
    else {
      val yarnRestfulUrl =
        SPARK_ONCE_YARN_RESTFUL_URL.getValue(EngineConnServer.getEngineCreationContext.getOptions)
      val getAppUrl = s"$yarnRestfulUrl/ws/v1/cluster/apps/$applicationId"
      logger.info(s"get yarn app, url: $getAppUrl")
      val appResult =
        JsonUtils.jackson.readValue(get(getAppUrl), classOf[java.util.Map[String, Object]])
      val app = appResult.get("app").asInstanceOf[java.util.Map[String, Object]]
      if (app == null) return Array.empty
      val trackingUrl = app.getOrDefault("trackingUrl", "").asInstanceOf[String]
      val state = app.getOrDefault("state", "").asInstanceOf[String]
      if (StringUtils.isBlank(trackingUrl) || "FINISHED".equals(state)) {
        return Array.empty
      }
      val getSparkJobsUrl = s"${trackingUrl}api/v1/applications/$applicationId/jobs"
      logger.info(s"get spark jobs, url: $getSparkJobsUrl")
      val jobs = get(getSparkJobsUrl)
      if (StringUtils.isBlank(jobs)) {
        return Array.empty
      }
      JsonUtils.jackson.readValue(
        get(getSparkJobsUrl),
        classOf[Array[java.util.Map[String, Object]]]
      )
    }

  def getKubernetesSparkJobInfo(
      applicationId: String,
      podIP: String
  ): Array[java.util.Map[String, Object]] =
    if (StringUtils.isBlank(applicationId) || StringUtils.isBlank(podIP)) Array.empty
    else {
      val getSparkJobsStateUrl = s"http://$podIP:4040/api/v1/applications/$applicationId"
      logger.info(s"get spark job state from kubernetes spark ui, url: $getSparkJobsStateUrl")
      val appStateResult =
        JsonUtils.jackson.readValue(
          get(getSparkJobsStateUrl),
          classOf[java.util.Map[String, Object]]
        )
      val appAttemptList = appStateResult.get("attempts").asInstanceOf[java.util.List[Object]]
      if (appAttemptList == null || appAttemptList.size() == 0) return Array.empty
      val appLastAttempt =
        appAttemptList.get(appAttemptList.size() - 1).asInstanceOf[util.Map[String, Object]]
      val isLastAttemptCompleted = appLastAttempt.get("completed").asInstanceOf[Boolean]
      if (isLastAttemptCompleted) return Array.empty
      val getSparkJobsInfoUrl = s"http://$podIP:4040/api/v1/applications/$applicationId/jobs"
      logger.info(s"get spark job info from kubernetes spark ui: $getSparkJobsInfoUrl")
      val jobs = get(getSparkJobsInfoUrl)
      if (StringUtils.isBlank(jobs)) {
        return Array.empty
      }
      JsonUtils.jackson.readValue(
        get(getSparkJobsInfoUrl),
        classOf[Array[java.util.Map[String, Object]]]
      )
    }

  def get(url: String): String = {
    val httpGet = new HttpGet(url)
    val client = HttpClients.createDefault
    val response = client.execute(httpGet)
    if (response.getStatusLine.getStatusCode == 200) {
      val str = EntityUtils.toString(response.getEntity, "UTF-8")
      logger.info(s"url: $url")
      logger.info(s"response: $str")
      str
    } else {
      ""
    }
  }

  private def getJobId(jobId: Int, jobGroup: String): String =
    "jobId-" + jobId + "(" + jobGroup + ")"

}
