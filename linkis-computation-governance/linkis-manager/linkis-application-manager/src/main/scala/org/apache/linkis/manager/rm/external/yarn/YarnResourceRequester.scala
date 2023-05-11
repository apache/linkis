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

import org.apache.linkis.common.utils.{Logging, Utils}
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

import java.text.MessageFormat
import java.util
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import net.sf.json.JSONObject

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

    def maxEffectiveHandle(queueValue: Option[Any]): Option[YarnResource] = {
      val metrics = getResponseByUrl("metrics", rmWebAddress)
      val metricsResource = jsonReadTree(metrics).get("clusterMetrics")
      val totalMB = metricsResource.get("totalMB").asInt()
      val totalVirtualCores = metricsResource.get("totalVirtualCores").asInt()
      val totalResourceInfoResponse = (totalMB, totalVirtualCores)

      queueValue.map(r => {
        val effectiveResource = jsonReadTree(r).get("absoluteCapacity").asDouble()

        new YarnResource(
          math
            .floor(effectiveResource * totalResourceInfoResponse._1 * 1024L * 1024L / 100)
            .toLong,
          math.floor(effectiveResource * totalResourceInfoResponse._2 / 100).toInt,
          0,
          queueName
        )
      })
    }

    var realQueueName = "root." + queueName

    def getQueue(queues: Any): Option[Any] = {
      if (queues.asInstanceOf[ArrayNode].size() > 0) {
        queues.asInstanceOf[ArrayNode].asScala.foreach { q =>
          val yarnQueueName = jsonReadTree(q).get("queueName").asText()
          if (yarnQueueName == realQueueName) return Some(q)
          else if (realQueueName.startsWith(yarnQueueName + ".")) {
            return getQueue(getChildQueues(q))
          }
        }
        None
      } else if (queues.isInstanceOf[util.Map[Any, Any]]) {
        if (
            queues
              .asInstanceOf[util.Map[Any, Any]]
              .asScala
              .find(_._1 == "queueName")
              .exists(_._2.toString == realQueueName)
        ) {
          Some(queues)
        } else {
          val childQueues =
            queues.asInstanceOf[util.Map[Any, Any]].asScala.find(_._1 == "childQueues")
          if (childQueues.isEmpty) None
          else getQueue(childQueues.map(_._2).get)
        }
      } else {
        None
      }
    }

    def getChildQueues(resp: Any): Any = {
      val childQueues = jsonReadTree(resp).get("childQueues")
      val childQueue = childQueues.get("queue")

      if (childQueue != null && childQueue.size() > 0) {
        logger.info(s"queues:$childQueue")
        childQueue
      } else {
        childQueues
      }
    }

    def getQueueOfCapacity(queues: Any): Option[Any] = {
      if (queues.isInstanceOf[ArrayNode]) {
        queues.asInstanceOf[ArrayNode].asScala.foreach { q =>
          val jsonObject = jsonReadTree(q)
          val yarnQueueName = jsonObject.get("queueName").asText()
          val queues = jsonObject.get("queues")
          if (yarnQueueName == realQueueName) return Some(q)
          else if (queues != null && queues.size() > 0) {
            val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(q))
            if (matchQueue.nonEmpty) return Some(matchQueue)
          }
        }
        None
      } else if (queues.isInstanceOf[util.Map[Any, Any]]) {
        val queuesValue = queues.asInstanceOf[util.Map[Any, Any]].get("queues")
        if (
            queues
              .asInstanceOf[util.Map[Any, Any]]
              .asScala
              .find(_._1 == "queueName")
              .exists(_._2.toString == realQueueName)
        ) {
          return Some(queues)
        } else if (queuesValue != null) {
          val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(queues))
          if (matchQueue.nonEmpty) return matchQueue
        }
        None
      } else {
        None
      }
    }

    def getChildQueuesOfCapacity(resp: Any) = {
      jsonReadTree(resp).get("queues").get("queue")
    }

    def getResources() = {
      val resp = getResponseByUrl("scheduler", rmWebAddress)
      val schedulerInfo = jsonReadTree(resp).get("scheduler")
      val schedulerInfoValue = schedulerInfo.get("schedulerInfo")
      val schedulerType = schedulerInfoValue.get("type").asText()

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

        val usedResource = jsonReadTree(queue.get).get("resourcesUsed")
        val usedMemory = usedResource.get("memory").asInt()
        val usedvCores = usedResource.get("vCores").asInt()

        val resourcesUsed = new YarnResource(usedMemory * 1024L * 1024L, usedvCores, 0, queueName)
        (maxEffectiveHandle(queue).get, resourcesUsed)
      } else if ("fairScheduler".equals(schedulerType)) {
        val rootQueueValue = schedulerInfoValue.get("rootQueue")
        val childQueues = getChildQueues(rootQueueValue)
        val queue = getQueue(childQueues)

        if (queue.isEmpty) {
          logger.debug(s"cannot find any information about queue $queueName, response: " + resp)
          throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode,
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc, queueName)
          )
        }
        val queueInfo = jsonReadTree(queue.get.toString)
        val maxResourceMemory = queueInfo.get("maxResources").get("memory").asInt()
        val maxResourcevCores = queueInfo.get("maxResources").get("vCores").asInt()

        val usedResourceMemory = queueInfo.get("usedResources").get("memory").asInt()
        val usedResourcevCores = queueInfo.get("usedResources").get("vCores").asInt()

        val maxResources =
          new YarnResource(maxResourceMemory * 1024L * 1024L, maxResourcevCores, 0, queueName)

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

    val realQueueName = "root." + queueName

    def getAppInfos(): Array[ExternalAppInfo] = {
      val response = getResponseByUrl("apps", rmWebAddress)
      val apps = jsonReadTree(response).get("apps")
      val app = apps.get("app")

      if (app.isInstanceOf[ArrayNode]) {
        val appInfoBuffer = new ArrayBuffer[YarnAppInfo]()
        app.asInstanceOf[ArrayNode].asScala.foreach { app =>
          val appInfo = jsonReadTree(app)
          val queueValue = appInfo.get("queue").asText()
          val yarnQueueName = queueValue
          val stateValue = appInfo.get("state").asText()
          val state = stateValue
          val idValue = appInfo.get("id").asText()
          val userValue = appInfo.get("user").asText()
          val applicationTypeValue = appInfo.get("applicationType").asText()

          val allocatedMB = appInfo.get("allocatedMB").asInt()
          val allocatedVCores = appInfo.get("allocatedVCores").asInt()
          val yarnResource =
            new YarnResource(allocatedMB * 1024L * 1024L, allocatedVCores, 0, queueName)

          val appInfoValue =
            YarnAppInfo(idValue, userValue, state, applicationTypeValue, yarnResource)
          if (yarnQueueName == realQueueName && (state == "RUNNING" || state == "ACCEPTED")) {
            appInfoBuffer.append(appInfoValue)
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
    var httpResponse: HttpResponse = null
    val authorEnable: Any = this.provider.getConfigMap.get("authorEnable")
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
    val resp1 = EntityUtils.toString(httpResponse.getEntity())
    resp1
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
                val info = getResponseByUrl("info", address)
                val haState = jsonReadTree(info).get("clusterInfo").get("haState").asText()

                if (haState.isInstanceOf[String]) {
                  if (HASTATE_ACTIVE.equalsIgnoreCase(haState.toString)) {
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

  private def jsonReadTree(url: Any) = {
    new ObjectMapper().readTree(JSONObject.fromObject(url.toString).toString)
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
