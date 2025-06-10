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

package org.apache.linkis.entrance.utils

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{Logging, SHAUtils, Utils}
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.governance.common.protocol.conf.{DepartmentRequest, DepartmentResponse}
import org.apache.linkis.instance.label.client.InstanceLabelClient
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.constant.{LabelKeyConstant, LabelValueConstant}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.manager.label.utils.EngineTypeLabelCreator
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.util.EntityUtils

import java.nio.charset.StandardCharsets
import java.util
import java.util.{HashMap, Map}

import scala.collection.JavaConverters.asScalaBufferConverter

object EntranceUtils extends Logging {

  private val SPLIT = ","

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  val sparkVersionRegex = "^3(\\.\\d+)*$"

  var DOCTOR_NONCE = "12345"

  def getUserCreatorEcTypeKey(
      userCreatorLabel: UserCreatorLabel,
      engineTypeLabel: EngineTypeLabel
  ): String = {
    userCreatorLabel.getStringValue + SPLIT + engineTypeLabel.getStringValue
  }

  def fromKeyGetLabels(key: String): (UserCreatorLabel, EngineTypeLabel) = {
    if (StringUtils.isBlank(key)) (null, null)
    else {
      val labelStringValues = key.split(SPLIT)
      if (labelStringValues.length < 2) return (null, null)
      val userCreatorLabel = labelFactory
        .createLabel[UserCreatorLabel](LabelKeyConstant.USER_CREATOR_TYPE_KEY, labelStringValues(0))
      val engineTypeLabel = labelFactory
        .createLabel[EngineTypeLabel](LabelKeyConstant.ENGINE_TYPE_KEY, labelStringValues(1))
      (userCreatorLabel, engineTypeLabel)
    }
  }

  def getDefaultCreatorECTypeKey(creator: String, ecType: String): String = {
    val userCreatorLabel =
      labelFactory.createLabel[UserCreatorLabel](LabelKeyConstant.USER_CREATOR_TYPE_KEY)
    val ecTypeLabel = EngineTypeLabelCreator.createEngineTypeLabel(ecType)
    userCreatorLabel.setUser("*")
    userCreatorLabel.setCreator(creator)
    getUserCreatorEcTypeKey(userCreatorLabel, ecTypeLabel)
  }

  def getRunningEntranceNumber(): Int = {
    val entranceNum = Sender.getInstances(Sender.getThisServiceInstance.getApplicationName).length
    val labelList = new util.ArrayList[Label[_]]()
    val offlineRouteLabel = LabelBuilderFactoryContext.getLabelBuilderFactory
      .createLabel[RouteLabel](LabelKeyConstant.ROUTE_KEY, LabelValueConstant.OFFLINE_VALUE)
    labelList.add(offlineRouteLabel)
    var offlineIns: Array[ServiceInstance] = null
    Utils.tryAndWarn {
      offlineIns = InstanceLabelClient.getInstance
        .getInstanceFromLabel(labelList)
        .asScala
        .filter(l =>
          null != l && l.getApplicationName
            .equalsIgnoreCase(Sender.getThisServiceInstance.getApplicationName)
        )
        .toArray
    }
    val entranceRealNumber = if (null != offlineIns) {
      logger.info(s"There are ${offlineIns.length} offlining instance.")
      entranceNum - offlineIns.length
    } else {
      entranceNum
    }
    /*
    Sender.getInstances may get 0 instances due to cache in Sender. So this instance is the one instance.
     */
    if (entranceRealNumber <= 0) {
      logger.error(
        s"Got ${entranceRealNumber} ${Sender.getThisServiceInstance.getApplicationName} instances."
      )
      1
    } else {
      entranceRealNumber
    }
  }

  def getUserDeapartmentId(username: String): String = {
    var departmentId = ""
    val sender: Sender =
      Sender.getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
    val responseSubmitUser = sender.ask(new DepartmentRequest(username))
    responseSubmitUser match {
      case departmentSubmitUser: DepartmentResponse =>
        if (StringUtils.isNotBlank(departmentSubmitUser.departmentId)) {
          departmentId = departmentSubmitUser.departmentId
        }
      case _ =>
    }
    departmentId
  }

  def getDynamicEngineType(sql: String, logAppender: java.lang.StringBuilder): String = {
    var engineType = "spark"
    if (!EntranceConfiguration.AI_SQL_DYNAMIC_ENGINE_SWITCH) {
      return engineType
    }
    // 参数校验
    if (
        StringUtils.isBlank(EntranceConfiguration.LINKIS_SYSTEM_NAME) ||
        StringUtils.isBlank(EntranceConfiguration.DOCTOR_SIGNATURE_TOKEN) ||
        StringUtils.isBlank(EntranceConfiguration.DOCTOR_CLUSTER) ||
        StringUtils.isBlank(EntranceConfiguration.DOCTOR_URL)
    ) {
      return engineType
    }
    // 组装请求url
    var printlog = s"Dynamic engine switching, using the engine's default values：$engineType"
    var url = EntranceConfiguration.DOCTOR_URL + EntranceConfiguration.DOCTOR_DYNAMIC_ENGINE_URL
    val timestampStr = String.valueOf(System.currentTimeMillis)
    val signature = SHAUtils.Encrypt(
      SHAUtils.Encrypt(
        EntranceConfiguration.LINKIS_SYSTEM_NAME + DOCTOR_NONCE + timestampStr,
        null
      ) + EntranceConfiguration.DOCTOR_SIGNATURE_TOKEN,
      null
    )
    url = url
      .replace("$app_id", EntranceConfiguration.LINKIS_SYSTEM_NAME)
      .replace("$timestamp", timestampStr)
      .replace("$nonce", DOCTOR_NONCE)
      .replace("$signature", signature)
    // 组装请求
    val httpPost = new HttpPost(url)
    val parm = new util.HashMap[String, AnyRef]
    parm.put("sql", sql)
    parm.put("highStability", "")
    parm.put("queueResourceUsage", "")
    parm.put("cluster", EntranceConfiguration.DOCTOR_CLUSTER)
    val json = BDPJettyServerHelper.gson.toJson(parm)
    val requestConfig = RequestConfig
      .custom()
      .setConnectTimeout(EntranceConfiguration.DOCTOR_REQUEST_TIMEOUT)
      .build()
    val entity = new StringEntity(
      json,
      ContentType.create(ContentType.APPLICATION_JSON.getMimeType, StandardCharsets.UTF_8.toString)
    )
    entity.setContentEncoding(StandardCharsets.UTF_8.toString)
    httpPost.setConfig(requestConfig)
    httpPost.setEntity(entity)
    val httpClient = HttpClients.createDefault
    try {
      val startTime = System.currentTimeMillis()
      val execute = httpClient.execute(httpPost)
      // 请求结果处理
      val responseStr: String =
        EntityUtils.toString(execute.getEntity, StandardCharsets.UTF_8.toString)
      val endTime = System.currentTimeMillis()
      val responseMapJson: Map[String, Object] =
        BDPJettyServerHelper.gson.fromJson(responseStr, classOf[Map[_, _]])
      if (MapUtils.isNotEmpty(responseMapJson) && responseMapJson.containsKey("data")) {
        val dataMap = MapUtils.getMap(responseMapJson, "data")
        engineType = dataMap.get("engine").toString
        val duration = (endTime - startTime) / 1000.0 // 计算耗时（单位：秒）
        printlog =
          s"Dynamic engine switching, Doctoris returns： $engineType ,Http call duration: $duration seconds"
      }
    } catch {
      case e: Exception =>
        logger.warn(s"调用Doctoris diagnose接口失败：sql: $sql", e)
        printlog =
          s"Dynamic engine switching exception, using the engine's default values：$engineType"
    } finally {
      httpClient.close()
      logAppender.append(LogUtils.generateWarn(s"$printlog\n"))
    }
    engineType
  }

}
