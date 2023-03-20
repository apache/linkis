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

package org.apache.linkis.manager.rm.external.yarn

import org.apache.linkis.common.utils.{JsonUtils, Logging, Utils}
import org.apache.linkis.manager.common.conf.RMConfiguration
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  NodeResource,
  ResourceType,
  YarnResource
}
import org.apache.linkis.manager.common.errorcode.ManagerCommonErrorCodeSummary._
import org.apache.linkis.manager.common.exception.{RMErrorException, RMWarnException}
import org.apache.linkis.manager.rm.external.domain.{
  ExternalAppInfo,
  ExternalResourceIdentifier,
  ExternalResourceProvider
}
import org.apache.linkis.manager.rm.external.request.ExternalResourceRequester
import org.apache.linkis.manager.rm.utils.RequestKerberosUrlUtils

import org.apache.commons.lang3.StringUtils
import org.apache.http.{HttpHeaders, HttpResponse}
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.json

import java.text.MessageFormat
import java.util
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.ReadContext

class YarnResourceRequester extends ExternalResourceRequester with Logging {

  private val HASTATE_ACTIVE = "active"

  private var provider: ExternalResourceProvider = _
  private val rmAddressMap: util.Map[String, String] = new ConcurrentHashMap[String, String]()

  private def getAuthorizationStr = {
    val user = this.provider.getConfigMap.getOrDefault("user", "").asInstanceOf[String]
    val pwd = this.provider.getConfigMap.getOrDefault("pwd", "").asInstanceOf[String]
    val authKey = user + ":" + pwd
    Base64.getMimeEncoder.encodeToString(authKey.getBytes)
  }

  override def requestResourceInfo(
      identifier: ExternalResourceIdentifier,
      provider: ExternalResourceProvider
  ): NodeResource = {
    val rmWebHaAddress = provider.getConfigMap.get("rmWebAddress").asInstanceOf[String]
    this.provider = provider
    val rmWebAddress = getAndUpdateActiveRmWebAddress(rmWebHaAddress)
    logger.info(s"rmWebAddress: $rmWebAddress")
    val queueName = identifier.asInstanceOf[YarnResourceIdentifier].getQueueName

    def getYarnResource(resource: Option[Any]) = resource.map(r => {
      val ctx = JsonPath.parse(r)
      val memory = ctx.read("$.memory").asInstanceOf[Long]
      val vCores = ctx.read("$.vCores").asInstanceOf[Int]
      new YarnResource(memory * 1024L * 1024L, vCores, 0, queueName)
    })

    def maxEffectiveHandle(queueValue: Option[Any]): Option[YarnResource] = {
      val metrics = getResponseByUrl("metrics", rmWebAddress)
      val ctx = JsonPath.parse(metrics)
      val totalMB = ctx.read("$.clusterMetrics.totalMB").asInstanceOf[Long]
      val totalVirtualCores =
        ctx.read("$.clusterMetrics.totalVirtualCores").asInstanceOf[Long]
      val totalResouceInfoResponse = (totalMB, totalVirtualCores)

      queueValue.map(r => {
        val absoluteCapacity = JsonPath.read(r, "$.absoluteCapacity")

        val effectiveResource = {
          if (absoluteCapacity.isInstanceOf[BigDecimal]) {
            absoluteCapacity.asInstanceOf[BigDecimal].toDouble
          } else if (absoluteCapacity.isInstanceOf[Double]) {
            absoluteCapacity.asInstanceOf[Double]
          } else {
            0d
          }
        }.asInstanceOf[Long]

        new YarnResource(
          math
            .floor(effectiveResource * totalResouceInfoResponse._1 * 1024L * 1024L / 100)
            .toLong,
          math.floor(effectiveResource * totalResouceInfoResponse._2 / 100).toInt,
          0,
          queueName
        )
      })
    }

    var realQueueName = "root." + queueName

    def getQueue(queues: Any): Option[Any] = {
      if (queues.isInstanceOf[List[Any]]) {
        queues.asInstanceOf[List[Any]].foreach { q =>
          val yarnQueueName = JsonPath.read(q, "$.queueName").asInstanceOf[String]
          if (yarnQueueName == realQueueName) return Some(q)
          else if (realQueueName.startsWith(yarnQueueName + ".")) {
            return getQueue(getChildQueues(q))
          }
        }
        None
      } else if (queues.isInstanceOf[Map[Any, Any]]) {
        if (
            queues
              .asInstanceOf[Map[Any, Any]]
              .find(_._1 == "queueName")
              .exists(_._2.toString == realQueueName)
        ) {
          Some(queues)
        } else {
          val childQueues = queues.asInstanceOf[Map[Any, Any]].find(_._1 == "childQueues")
          if (childQueues.isEmpty) None
          else getQueue(childQueues.map(_._2).get)
        }
      } else {
        None
      }
    }

    def getChildQueues(resp: Any): Any = {
      val ctx = JsonPath.parse(resp)
      val childQueuesValue = ctx.read("$.childQueues")
      val queues =
        ctx.read("$.childQueues.queue").asInstanceOf[List[Any]]

      if (queues != null && queues.nonEmpty) {
        logger.info(s"queues:$queues")
        queues
      } else childQueuesValue
    }

    def getQueueOfCapacity(queues: Any): Option[Any] = {
      if (queues.isInstanceOf[List[Any]]) {
        queues.asInstanceOf[List[Any]].foreach { q =>
          val ctx = JsonPath.parse(q)
          val yarnQueueName = ctx.read("$.queueName").asInstanceOf[String]
          val queuesValue = ctx.read("$.queues").asInstanceOf[String]

          if (yarnQueueName == realQueueName) return Some(q)
          else if (queuesValue.nonEmpty) {
            val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(q))
            if (matchQueue.nonEmpty) return matchQueue
          }
        }
        None
      } else if (queues.isInstanceOf[Map[Any, Any]]) {
        val queuesValue = JsonPath.read(queues, "$.queues").asInstanceOf[String]
        if (
            queues
              .asInstanceOf[Map[Any, Any]]
              .find(_._1 == "queueName")
              .exists(_._2.toString == realQueueName)
        ) {
          return Some(queues)
        } else if (queuesValue.nonEmpty) {
          val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(queues.toString))
          if (matchQueue.nonEmpty) return matchQueue
        }
        None
      } else {
        None
      }
    }

    def getChildQueuesOfCapacity(resp: Any) = {
      JsonPath.read(resp, "$.queues.queue").asInstanceOf[String]
    }

    def getResources() = {
      val resp = getResponseByUrl("scheduler", rmWebAddress)
      val ctx = JsonPath.parse(resp)
      val schedulerInfoValue =
        ctx.read("$.scheduler.schedulerInfo").asInstanceOf[String]
      val schedulerType =
        ctx.read("$.scheduler.schedulerInfo.type").asInstanceOf[String]
      val rootQueueValue =
        ctx.read("$.scheduler.schedulerInfo.rootQueue").asInstanceOf[String]

      if ("capacityScheduler".equals(schedulerType)) {
        realQueueName = queueName
        val childQueues = getChildQueuesOfCapacity(schedulerInfoValue)
        val queue = getQueueOfCapacity(childQueues)
        if (queue.isEmpty) {
          logger.debug(s"cannot find any information about queue $queueName, response: " + resp)
          throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode,
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc, queueName)
          )
        }

        val resourceCtx = JsonPath.parse(queue)
        val usedMemory = resourceCtx.read("$.resourcesUsed.memory").asInstanceOf[Long]
        val usedvCores = resourceCtx.read("$.resourcesUsed.vCores").asInstanceOf[Int]
        val resourcesUsed = new YarnResource(usedMemory * 1024L * 1024L, usedvCores, 0, queueName)

        (maxEffectiveHandle(queue).get, resourcesUsed)
      } else if ("fairScheduler".equals(schedulerType)) {
        val childQueues = getChildQueues(rootQueueValue)
        val queue = getQueue(childQueues)
        if (queue.isEmpty) {
          logger.debug(s"cannot find any information about queue $queueName, response: " + resp)
          throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode,
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc, queueName)
          )
        }
        val resourceCtx = JsonPath.parse(queue)
        val maxResourceMemory = resourceCtx.read("$.maxResources.memory").asInstanceOf[Long]
        val maxResourcevCores = resourceCtx.read("$.maxResources.vCores").asInstanceOf[Int]
        val maxResources =
          new YarnResource(maxResourceMemory * 1024L * 1024L, maxResourcevCores, 0, queueName)

        val usedResourceMemory = resourceCtx.read("$.usedResources.memory").asInstanceOf[Long]
        val usedResourcevCores = resourceCtx.read("$.usedResources.vCores").asInstanceOf[Int]
        val usedResourcesUsed =
          new YarnResource(usedResourceMemory * 1024L * 1024L, usedResourcevCores, 0, queueName)

        (maxResources, usedResourcesUsed)
      } else {
        logger.debug(
          s"only support fairScheduler or capacityScheduler, schedulerType: $schedulerType , response: " + resp
        )
        throw new RMWarnException(
          ONLY_SUPPORT_FAIRORCAPA.getErrorCode,
          MessageFormat.format(ONLY_SUPPORT_FAIRORCAPA.getErrorDesc(), schedulerType)
        )
      }
    }

    Utils.tryCatch {
      val yarnResource = getResources()
      val nodeResource = new CommonNodeResource
      nodeResource.setMaxResource(yarnResource._1)
      nodeResource.setUsedResource(yarnResource._2)
      nodeResource
    }(t => {
      throw new RMErrorException(
        YARN_QUEUE_EXCEPTION.getErrorCode,
        YARN_QUEUE_EXCEPTION.getErrorDesc,
        t
      )
    })
  }

  override def requestAppInfo(
      identifier: ExternalResourceIdentifier,
      provider: ExternalResourceProvider
  ): java.util.List[ExternalAppInfo] = {
    val rmWebHaAddress = provider.getConfigMap.get("rmWebAddress").asInstanceOf[String]

    val rmWebAddress = getAndUpdateActiveRmWebAddress(rmWebHaAddress)

    val queueName = identifier.asInstanceOf[YarnResourceIdentifier].getQueueName

    def getYarnResource(resource: Option[Any]) = resource.map(r => {
      val ctx = JsonPath.parse(r)
      val allocatedMB = ctx.read("$.allocatedMB").asInstanceOf[Long]
      val allocatedVCores = ctx.read("$.allocatedVCores").asInstanceOf[Int]
      new YarnResource(allocatedMB * 1024L * 1024L, allocatedVCores, 0, queueName)
    })

    val realQueueName = "root." + queueName

    def getAppInfos(): Array[ExternalAppInfo] = {
      val resp = getResponseByUrl("apps", rmWebAddress)
      val ctx = JsonPath.parse(resp)
      val apps = ctx.read("$.apps")
      val app = ctx.read("$.apps.app")

      if (app.isInstanceOf[List[Any]]) {
        val appInfoBuffer = new ArrayBuffer[YarnAppInfo]()
        apps.asInstanceOf[List[Any]].foreach { app =>
          val appCtx = JsonPath.parse(app)
          val queueValue = appCtx.read("$.queue").asInstanceOf[String]
          val stateValue = appCtx.read("$.state").asInstanceOf[String]
          val idValue = appCtx.read("$.id").asInstanceOf[String]
          val userValue = appCtx.read("$.user").asInstanceOf[String]
          val applicationTypeValue =
            appCtx.read("$.applicationType").asInstanceOf[String]
          val yarnQueueName = queueValue
          val state = stateValue
          if (yarnQueueName == realQueueName && (state == "RUNNING" || state == "ACCEPTED")) {
            val appInfo = YarnAppInfo(
              idValue,
              userValue,
              state,
              applicationTypeValue,
              getYarnResource(Some(app.asInstanceOf[YarnResource])).get
            )
            appInfoBuffer.append(appInfo)
          }
        }
        appInfoBuffer.toArray
      } else {
        new ArrayBuffer[YarnAppInfo](0).toArray
      }
    }

    Utils.tryCatch(getAppInfos().toList.asJava)(t => {
      throw new RMErrorException(
        YARN_APPLICATION_EXCEPTION.getErrorCode,
        YARN_APPLICATION_EXCEPTION.getErrorDesc,
        t
      )
    })
  }

  override def getResourceType: ResourceType = ResourceType.Yarn

  private def getResponseByUrl(url: String, rmWebAddress: String) = {
    val httpGet = new HttpGet(rmWebAddress + "/ws/v1/cluster/" + url)
    httpGet.addHeader("Accept", "application/json")
    val authorEnable: Any = this.provider.getConfigMap.get("authorEnable")
    var httpResponse: HttpResponse = null
    authorEnable match {
      case flag: Boolean =>
        if (flag) {
          httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationStr)
        }
      case _ =>
    }
    val kerberosEnable: Any = this.provider.getConfigMap.get("kerberosEnable")
    kerberosEnable match {
      case flag: Boolean =>
        if (flag) {
          val principalName = this.provider.getConfigMap.get("principalName").asInstanceOf[String]
          val keytabPath = this.provider.getConfigMap.get("keytabPath").asInstanceOf[String]
          val krb5Path = this.provider.getConfigMap.get("krb5Path").asInstanceOf[String]
          val requestKuu = new RequestKerberosUrlUtils(principalName, keytabPath, krb5Path, false)
          val response =
            requestKuu.callRestUrl(rmWebAddress + "/ws/v1/cluster/" + url, principalName)
          httpResponse = response
        } else {
          val response = YarnResourceRequester.httpClient.execute(httpGet)
          httpResponse = response
        }
      case _ =>
        val response = YarnResourceRequester.httpClient.execute(httpGet)
        httpResponse = response
    }
    JsonUtils.jackson.readValue(
      EntityUtils.toString(httpResponse.getEntity()),
      classOf[Map[String, Any]]
    )
  }

  def getAndUpdateActiveRmWebAddress(haAddress: String): String = {
    // todo check if it will stuck for many requests
    var activeAddress = rmAddressMap.get(haAddress)
    if (StringUtils.isBlank(activeAddress)) haAddress.intern().synchronized {
      if (StringUtils.isBlank(activeAddress)) {
        if (logger.isDebugEnabled()) {
          logger.debug(
            s"Cannot find value of haAddress : ${haAddress} in cacheMap with size ${rmAddressMap.size()}"
          )
        }
        if (StringUtils.isNotBlank(haAddress)) {
          haAddress
            .split(RMConfiguration.DEFAULT_YARN_RM_WEB_ADDRESS_DELIMITER.getValue)
            .foreach(address => {
              Utils.tryCatch {
                val response = getResponseByUrl("info", address)
                val haState = JsonPath.read(response, "$.clusterInfo.haState")

                if (haState.isInstanceOf[String]) {
                  if (HASTATE_ACTIVE.equalsIgnoreCase(haState)) {
                    activeAddress = address
                  } else {
                    logger.warn(s"Resourcemanager : ${address} haState : ${haState}")
                  }
                }
              } { case e: Exception =>
                logger.error("Get Yarn resourcemanager info error, " + e.getMessage, e)
              }
            })
        }
        if (StringUtils.isNotBlank(activeAddress)) {
          if (logger.isDebugEnabled()) {
            logger.debug(s"Put (${haAddress}, ${activeAddress}) to cacheMap.")
          }
          rmAddressMap.put(haAddress, activeAddress)
        } else {
          throw new RMErrorException(
            GET_YARN_EXCEPTION.getErrorCode,
            MessageFormat.format(GET_YARN_EXCEPTION.getErrorDesc(), haAddress)
          )
        }
      }
    }
    if (logger.isDebugEnabled()) {
      logger.debug(s"Get active rm address : ${activeAddress} from haAddress : ${haAddress}")
    }
    activeAddress
  }

  override def reloadExternalResourceAddress(
      provider: ExternalResourceProvider
  ): java.lang.Boolean = {
    if (null == provider) {
      rmAddressMap.clear()
    } else {
      val rmWebHaAddress = provider.getConfigMap.get("rmWebAddress").asInstanceOf[String]
      rmAddressMap.remove(rmWebHaAddress)
      getAndUpdateActiveRmWebAddress(rmWebHaAddress)
    }
    true
  }

}

object YarnResourceRequester extends Logging {

  private val httpClient = HttpClients.createDefault()

  def init(): Unit = {
    addShutdownHook()
  }

  def addShutdownHook(): Unit = {
    logger.info("Register shutdown hook to release httpclient connection")
    Utils.addShutdownHook(httpClient.close())
  }

}
