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
import org.apache.linkis.entrance.errorcode.EntranceErrorCodeSummary
import org.apache.linkis.entrance.exception.EntranceRPCException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{DepartmentRequest, DepartmentResponse}
import org.apache.linkis.instance.label.client.InstanceLabelClient
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.constant.{LabelKeyConstant, LabelValueConstant}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineType, EngineTypeLabel, UserCreatorLabel}
import org.apache.linkis.manager.label.entity.route.RouteLabel
import org.apache.linkis.manager.label.utils.{EngineTypeLabelCreator, LabelUtil}
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.collections.MapUtils
import org.apache.commons.lang3.StringUtils
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{CloseableHttpResponse, HttpPost}
import org.apache.http.entity.{ContentType, StringEntity}
import org.apache.http.impl.client.{BasicCookieStore, CloseableHttpClient, HttpClients}
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils

import java.{lang, util}
import java.nio.charset.StandardCharsets
import java.util.{HashMap, Map}

import scala.collection.JavaConverters.asScalaBufferConverter

object EntranceUtils extends Logging {

  private val SPLIT = ","

  private val labelFactory = LabelBuilderFactoryContext.getLabelBuilderFactory

  val sparkVersionRegex = "^3(\\.\\d+)*$"

  protected val connectionManager = new PoolingHttpClientConnectionManager
  protected val cookieStore = new BasicCookieStore

  private val httpClient: CloseableHttpClient = HttpClients
    .custom()
    .setDefaultCookieStore(cookieStore)
    .setMaxConnTotal(EntranceConfiguration.DOCTOR_HTTP_MAX_CONNECT)
    .setMaxConnPerRoute(EntranceConfiguration.DOCTOR_HTTP_MAX_CONNECT / 2)
    .setConnectionManager(connectionManager)
    .build()

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

  /**
   * 获取用户部门ID
   */
  def getUserDepartmentId(username: String): String = {
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

  /**
   * 动态引擎类型选择
   */
  def getDynamicEngineType(sql: String, logAppender: java.lang.StringBuilder): String = {
    val defaultEngineType = "spark"

    if (!EntranceConfiguration.AI_SQL_DYNAMIC_ENGINE_SWITCH) {
      return defaultEngineType
    }

    logger.info(s"AISQL automatically switches engines and begins to call Doctoris")

    val params = new util.HashMap[String, AnyRef]()
    params.put("sql", sql)
    params.put("highStability", "")
    params.put("queueResourceUsage", "")

    val request = DoctorRequest(
      apiUrl = EntranceConfiguration.DOCTOR_DYNAMIC_ENGINE_URL,
      params = params,
      defaultValue = defaultEngineType,
      successMessage = "Aisql intelligent selection engines, Suggest",
      exceptionMessage = "Aisql intelligent selection component exception"
    )

    val response = callDoctorService(request, logAppender)
    response.result
  }

  def dealsparkDynamicConf(
      jobRequest: JobRequest,
      logAppender: lang.StringBuilder,
      params: util.Map[String, AnyRef]
  ): Unit = {
    // deal with spark3 dynamic allocation conf
    // 1.只有spark3需要处理动态规划参数 2.用户未指定模板名称，则设置默认值与spark底层配置保持一致，否则使用用户模板中指定的参数
    val properties = new util.HashMap[String, AnyRef]()
    val label: EngineTypeLabel = LabelUtil.getEngineTypeLabel(jobRequest.getLabels)
    val sparkDynamicAllocationEnabled: Boolean =
      EntranceConfiguration.SPARK_DYNAMIC_ALLOCATION_ENABLED
    if (
        sparkDynamicAllocationEnabled && label.getEngineType.equals(
          EngineType.SPARK.toString
        ) && label.getVersion.contains(LabelCommonConfig.SPARK3_ENGINE_VERSION.getValue)
    ) {
      properties.put(
        EntranceConfiguration.SPARK_EXECUTOR_CORES.key,
        EntranceConfiguration.SPARK_EXECUTOR_CORES.getValue
      )
      properties.put(
        EntranceConfiguration.SPARK_EXECUTOR_MEMORY.key,
        EntranceConfiguration.SPARK_EXECUTOR_MEMORY.getValue
      )
      properties.put(
        EntranceConfiguration.SPARK_DYNAMIC_ALLOCATION_MAX_EXECUTORS.key,
        EntranceConfiguration.SPARK_DYNAMIC_ALLOCATION_MAX_EXECUTORS.getValue
      )
      properties.put(
        EntranceConfiguration.SPARK_EXECUTOR_INSTANCES.key,
        EntranceConfiguration.SPARK_EXECUTOR_INSTANCES.getValue
      )
      properties.put(
        EntranceConfiguration.SPARK_EXECUTOR_MEMORY_OVERHEAD.key,
        EntranceConfiguration.SPARK_EXECUTOR_MEMORY_OVERHEAD.getValue
      )
      properties.put(
        EntranceConfiguration.SPARK3_PYTHON_VERSION.key,
        EntranceConfiguration.SPARK3_PYTHON_VERSION.getValue
      )
      Utils.tryAndWarn {
        val extraConfs: String =
          EntranceConfiguration.SPARK_DYNAMIC_ALLOCATION_ADDITIONAL_CONFS
        if (StringUtils.isNotBlank(extraConfs)) {
          val confs: Array[String] = extraConfs.split(",")
          for (conf <- confs) {
            val confKey: String = conf.split("=")(0)
            val confValue: String = conf.split("=")(1)
            properties.put(confKey, confValue)
          }
        }
      }
      logAppender.append(
        LogUtils
          .generateInfo(s"use spark3 default conf. \n")
      )
      TaskUtils.addStartupMap(params, properties)
    }
  }

  /**
   * 敏感信息SQL检查
   */
  def sensitiveSqlCheck(
      code: String,
      codeType: String,
      engine: String,
      user: String,
      logAppender: java.lang.StringBuilder
  ): (Boolean, String) = {
    val params = new util.HashMap[String, AnyRef]()
    params.put("code", code)
    params.put("user", user)
    params.put("engine", engine)
    params.put("codeType", codeType)

    val request = DoctorRequest(
      apiUrl = EntranceConfiguration.DOCTOR_ENCRYPT_SQL_URL,
      params = params,
      defaultValue = "false",
      successMessage = "Sensitive SQL Check result",
      exceptionMessage = "Sensitive SQL Check exception"
    )

    val response = callDoctorService(request, logAppender)
    (response.result.toBoolean, response.reason)
  }

  /**
   * Doctor服务调用通用框架
   */
  case class DoctorRequest(
      apiUrl: String,
      params: util.Map[String, AnyRef],
      defaultValue: String,
      successMessage: String,
      exceptionMessage: String
  )

  case class DoctorResponse(
      success: Boolean,
      result: String,
      reason: String = "",
      duration: Double = 0.0
  )

  /**
   * 通用Doctor服务调用方法
   */
  private def callDoctorService(
      request: DoctorRequest,
      logAppender: java.lang.StringBuilder
  ): DoctorResponse = {
    // 检查必要的配置参数
    if (!isValidDoctorConfiguration()) {
      logInfo(s"${request.exceptionMessage}, using default: ${request.defaultValue}", logAppender)
      return DoctorResponse(success = false, result = request.defaultValue)
    }

    try {
      val startTime = System.currentTimeMillis()
      val url = buildDoctorRequestUrl(request.apiUrl)
      val response = executeDoctorHttpRequest(url, request.params)

      if (StringUtils.isBlank(response)) {
        return DoctorResponse(success = false, result = request.defaultValue)
      }

      parseDoctorResponse(response, startTime, request, logAppender)
    } catch {
      case e: Exception =>
        logger.warn(s"${request.exceptionMessage}: params: ${request.params}", e)
        logInfo(s"${request.exceptionMessage}, using default: ${request.defaultValue}", logAppender)
        DoctorResponse(success = false, result = request.defaultValue)
    }
  }

  /**
   * 检查Doctor配置参数是否有效
   */
  private def isValidDoctorConfiguration(): Boolean = {
    StringUtils.isNotBlank(EntranceConfiguration.LINKIS_SYSTEM_NAME) &&
    StringUtils.isNotBlank(EntranceConfiguration.DOCTOR_SIGNATURE_TOKEN) &&
    StringUtils.isNotBlank(EntranceConfiguration.DOCTOR_CLUSTER) &&
    StringUtils.isNotBlank(EntranceConfiguration.DOCTOR_URL)
  }

  /**
   * 构建Doctor请求URL
   */
  private def buildDoctorRequestUrl(apiUrl: String): String = {
    val timestampStr = String.valueOf(System.currentTimeMillis)
    val signature = SHAUtils.Encrypt(
      SHAUtils.Encrypt(
        EntranceConfiguration.LINKIS_SYSTEM_NAME + EntranceConfiguration.DOCTOR_NONCE + timestampStr,
        null
      ) + EntranceConfiguration.DOCTOR_SIGNATURE_TOKEN,
      null
    )

    (EntranceConfiguration.DOCTOR_URL + apiUrl)
      .replace("$app_id", EntranceConfiguration.LINKIS_SYSTEM_NAME)
      .replace("$timestamp", timestampStr)
      .replace("$nonce", EntranceConfiguration.DOCTOR_NONCE)
      .replace("$signature", signature)
  }

  /**
   * 执行Doctor HTTP请求
   */
  private def executeDoctorHttpRequest(url: String, params: util.Map[String, AnyRef]): String = {
    val httpPost = new HttpPost(url)
    // 添加通用参数
    params.put("cluster", EntranceConfiguration.DOCTOR_CLUSTER)

    val json = BDPJettyServerHelper.gson.toJson(params)
    val requestConfig = RequestConfig
      .custom()
      .setConnectTimeout(EntranceConfiguration.DOCTOR_REQUEST_TIMEOUT)
      .setConnectionRequestTimeout(EntranceConfiguration.DOCTOR_REQUEST_TIMEOUT)
      .setSocketTimeout(EntranceConfiguration.DOCTOR_REQUEST_TIMEOUT)
      .build()

    val entity = new StringEntity(
      json,
      ContentType.create(ContentType.APPLICATION_JSON.getMimeType, StandardCharsets.UTF_8.toString)
    )
    entity.setContentEncoding(StandardCharsets.UTF_8.toString)
    httpPost.setConfig(requestConfig)
    httpPost.setEntity(entity)

    val execute = httpClient.execute(httpPost)
    EntityUtils.toString(execute.getEntity, StandardCharsets.UTF_8.toString)
  }

  /**
   * 解析Doctor响应结果
   */
  private def parseDoctorResponse(
      responseStr: String,
      startTime: Long,
      request: DoctorRequest,
      logAppender: java.lang.StringBuilder
  ): DoctorResponse = {
    try {
      val endTime = System.currentTimeMillis()
      val responseMapJson: Map[String, Object] =
        BDPJettyServerHelper.gson.fromJson(responseStr, classOf[Map[_, _]])

      if (MapUtils.isNotEmpty(responseMapJson) && responseMapJson.containsKey("data")) {
        val dataMap = MapUtils.getMap(responseMapJson, "data")
        val duration = (endTime - startTime) / 1000.0

        // 根据不同的API返回不同的结果
        if (request.apiUrl.contains("plaintext")) {
          // 敏感信息检查API
          val sensitive = dataMap.get("sensitive").toString.toBoolean
          val reason = dataMap.get("reason").toString
          logInfo(
            s"${request.successMessage}: $sensitive, This decision took $duration seconds",
            logAppender
          )
          DoctorResponse(
            success = true,
            result = sensitive.toString,
            reason = reason,
            duration = duration
          )
        } else {
          // 动态引擎选择API
          val engineType = dataMap.get("engine").toString
          val reason = dataMap.get("reason").toString
          logInfo(
            s"${request.successMessage}: $engineType, Hit rules: $reason, This decision took $duration seconds",
            logAppender
          )
          DoctorResponse(success = true, result = engineType, reason = reason, duration = duration)
        }
      } else {
        throw new EntranceRPCException(
          EntranceErrorCodeSummary.DOCTORIS_ERROR.getErrorCode,
          EntranceErrorCodeSummary.DOCTORIS_ERROR.getErrorDesc
        )
      }
    } catch {
      case e: Exception =>
        logger.warn(s"Doctoris返回数据解析失败：json: $responseStr", e)
        logInfo(s"${request.exceptionMessage}, using default: ${request.defaultValue}", logAppender)
        DoctorResponse(success = false, result = request.defaultValue)
    }
  }

  /**
   * 记录日志信息
   */
  private def logInfo(message: String, logAppender: java.lang.StringBuilder): Unit = {
    logAppender.append(LogUtils.generateInfo(s"$message\n"))
  }

}
