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

package org.apache.linkis.monitor.jobhistory.jobtime

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.monitor.constants.Constants
import org.apache.linkis.monitor.core.ob.Observer
import org.apache.linkis.monitor.core.pac.{AbstractScanRule, ScannedData}
import org.apache.linkis.monitor.jobhistory.entity.JobHistory
import org.apache.linkis.monitor.until.{CacheUtils, HttpsUntils}
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils

import java.util
import java.util.{ArrayList, HashMap, List, Locale, Map}

import scala.collection.JavaConverters._

class StarrocksTimeExceedRule(hitObserver: Observer)
    extends AbstractScanRule(event = new StarrocksTimeExceedHitEvent, observer = hitObserver)
    with Logging {

  /**
   * if data match the pattern, return true and trigger observer should call isMatched()
   *
   * @param data
   * @return
   */
  override def triggerIfMatched(data: util.List[ScannedData]): Boolean = {

    if (!getHitEvent().isRegistered || data == null) {
      logger.error("ScanRule is not bind with an observer. Will not be triggered")
      return false
    }
    val alertData: util.List[JobHistory] = new util.ArrayList[JobHistory]()
    for (scannedData <- data.asScala) {
      if (scannedData != null && scannedData.getData() != null) {
        for (jobHistory <- scannedData.getData().asScala) {
          jobHistory match {
            case job: JobHistory =>
              val status = job.getStatus.toUpperCase(Locale.getDefault)
              val engineType = job.getEngineType.toUpperCase(Locale.getDefault)
              if (
                  Constants.UNFINISHED_JOB_STATUS
                    .contains(status) && engineType.equals(
                    Constants.JDBC_ENGINE.toUpperCase(Locale.getDefault)
                  )
              ) {
                // 获取job所使用的数据源类型
                val datasourceConfMap = getDatasourceConf(job)
                logger.info("starock  datasourceConfMap: {}", datasourceConfMap)
                // 计算任务执行时间
                val elapse = System.currentTimeMillis() - job.getCreatedTime.getTime
                // 获取告警配置
                val timeValue =
                  HttpsUntils.getJDBCConf(job.getSubmitUser, Constants.JDBC_ALERT_TIME)
                logger.info("starock  timeValue: {},elapse   {}", timeValue, elapse)
                if (StringUtils.isNotBlank(timeValue)) {
                  val timeoutInSeconds = timeValue.toDouble
                  val timeoutInMillis = (timeoutInSeconds * 60 * 1000).toLong
                  if (elapse > timeoutInMillis) {
                    // 发送告警
                    alertData.add(job)
                  }
                }
                // 获取超时kill配置信息
                if (StringUtils.isNotBlank(job.getParams)) {
                  val connectParamsMap = MapUtils.getMap(
                    datasourceConfMap,
                    "connectParams",
                    new util.HashMap[AnyRef, AnyRef]
                  )
                  val killTime = MapUtils.getString(connectParamsMap, "kill_task_time", "")
                  logger.info("starock  killTime: {}", killTime)
                  if (StringUtils.isNotBlank(killTime) && elapse > killTime.toLong * 60 * 1000) {
                    if (StringUtils.isNotBlank(killTime)) {
                      val timeoutInSeconds = timeValue.toDouble
                      val timeoutInMillis = (timeoutInSeconds * 60 * 1000).toLong
                      if (elapse > timeoutInMillis) {
                        // 触发kill任务
                        HttpsUntils.killJob(job)
                      }
                    }
                  }
                }
//                }
              }
            case _ =>
              logger.warn(
                "Ignored wrong input data Type : " + jobHistory + ", " + jobHistory.getClass.getCanonicalName
              )
          }
        }
      } else {
        logger.warn("Ignored null scanned data")
      }

    }
    logger.info("hit " + alertData.size() + " data in one iteration")
    if (alertData.size() > 0) {
      getHitEvent.notifyObserver(getHitEvent, alertData)
      true
    } else {
      false
    }
  }

  private def getDatasourceConf(job: JobHistory): util.Map[_, _] = {
    // 获取任务参数中datasourcename
    val parmMap =
      BDPJettyServerHelper.gson.fromJson(job.getParams, classOf[java.util.Map[String, String]])
    val configurationMap =
      MapUtils.getMap(parmMap, "configuration", new util.HashMap[String, String]())
    val runtimeMap =
      MapUtils.getMap(configurationMap, "runtime", new util.HashMap[String, String]())
    val datasourceName = MapUtils.getString(runtimeMap, Constants.JOB_DATASOURCE_CONF, "")
    // 获取datasource信息
    if (StringUtils.isNotBlank(datasourceName)) {
      HttpsUntils.getDatasourceConf(job.getSubmitUser, datasourceName)
    } else {
      new util.HashMap[String, String]()
    }
  }

}
