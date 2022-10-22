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

import org.json4s.JsonAST._
import org.json4s.JValue
import org.json4s.jackson.JsonMethods.parse

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

    def getYarnResource(jValue: Option[JValue]) = jValue.map(r =>
      new YarnResource(
        (r \ "memory").asInstanceOf[JInt].values.toLong * 1024L * 1024L,
        (r \ "vCores").asInstanceOf[JInt].values.toInt,
        0,
        queueName
      )
    )

    def maxEffectiveHandle(queueValue: Option[JValue]): Option[YarnResource] = {
      val metrics = getResponseByUrl("metrics", rmWebAddress)
      val totalResouceInfoResponse = (
        (metrics \ "clusterMetrics" \ "totalMB").asInstanceOf[JInt].values.toLong,
        (metrics \ "clusterMetrics" \ "totalVirtualCores").asInstanceOf[JInt].values.toLong
      )
      queueValue.map(r => {
        val absoluteCapacity = r \ "absoluteCapacity" match {
          case jDecimal: JDecimal =>
            jDecimal.values.toDouble
          case jDouble: JDouble =>
            jDouble.values
          case _ =>
            0d
        }
        val effectiveResource = absoluteCapacity
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

    def getQueue(queues: JValue): Option[JValue] = queues match {
      case JArray(queue) =>
        queue.foreach { q =>
          val yarnQueueName = (q \ "queueName").asInstanceOf[JString].values
          if (yarnQueueName == realQueueName) return Some(q)
          else if (realQueueName.startsWith(yarnQueueName + ".")) {
            return getQueue(getChildQueues(q))
          }
        }
        None
      case JObject(queue) =>
        if (
            queue
              .find(_._1 == "queueName")
              .exists(_._2.asInstanceOf[JString].values == realQueueName)
        ) {
          Some(queues)
        } else {
          val childQueues = queue.find(_._1 == "childQueues")
          if (childQueues.isEmpty) None
          else getQueue(childQueues.map(_._2).get)
        }
      case _ => None
    }

    def getChildQueues(resp: JValue): JValue = {
      val queues = resp \ "childQueues" \ "queue"

      if (
          queues != null && queues != JNull && queues != JNothing && null != queues.children && queues.children.nonEmpty
      ) {
        logger.info(s"queues:$queues")
        queues
      } else resp \ "childQueues"
    }

    def getQueueOfCapacity(queues: JValue): Option[JValue] = queues match {
      case JArray(queue) =>
        queue.foreach { q =>
          val yarnQueueName = (q \ "queueName").asInstanceOf[JString].values
          if (yarnQueueName == realQueueName) return Some(q)
          else if ((q \ "queues").toOption.nonEmpty) {
            val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(q))
            if (matchQueue.nonEmpty) return matchQueue
          }
        }
        None
      case JObject(queue) =>
        if (
            queue
              .find(_._1 == "queueName")
              .exists(_._2.asInstanceOf[JString].values == realQueueName)
        ) {
          return Some(queues)
        } else if ((queues \ "queues").toOption.nonEmpty) {
          val matchQueue = getQueueOfCapacity(getChildQueuesOfCapacity(queues))
          if (matchQueue.nonEmpty) return matchQueue
        }
        None
      case _ => None
    }

    def getChildQueuesOfCapacity(resp: JValue) = resp \ "queues" \ "queue"

    def getResources() = {
      val resp = getResponseByUrl("scheduler", rmWebAddress)
      val schedulerType =
        (resp \ "scheduler" \ "schedulerInfo" \ "type").asInstanceOf[JString].values
      if ("capacityScheduler".equals(schedulerType)) {
        realQueueName = queueName
        val childQueues = getChildQueuesOfCapacity(resp \ "scheduler" \ "schedulerInfo")
        val queue = getQueueOfCapacity(childQueues)
        if (queue.isEmpty) {
          logger.debug(s"cannot find any information about queue $queueName, response: " + resp)
          throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode,
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc, queueName)
          )
        }
        (maxEffectiveHandle(queue).get, getYarnResource(queue.map(_ \ "resourcesUsed")).get)
      } else if ("fairScheduler".equals(schedulerType)) {
        val childQueues = getChildQueues(resp \ "scheduler" \ "schedulerInfo" \ "rootQueue")
        val queue = getQueue(childQueues)
        if (queue.isEmpty) {
          logger.debug(s"cannot find any information about queue $queueName, response: " + resp)
          throw new RMWarnException(
            YARN_NOT_EXISTS_QUEUE.getErrorCode,
            MessageFormat.format(YARN_NOT_EXISTS_QUEUE.getErrorDesc, queueName)
          )
        }
        (
          getYarnResource(queue.map(_ \ "maxResources")).get,
          getYarnResource(queue.map(_ \ "usedResources")).get
        )
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

    def getYarnResource(jValue: Option[JValue]) = jValue.map(r =>
      new YarnResource(
        (r \ "allocatedMB").asInstanceOf[JInt].values.toLong * 1024L * 1024L,
        (r \ "allocatedVCores").asInstanceOf[JInt].values.toInt,
        0,
        queueName
      )
    )

    val realQueueName = "root." + queueName

    def getAppInfos(): Array[ExternalAppInfo] = {
      val resp = getResponseByUrl("apps", rmWebAddress)
      resp \ "apps" \ "app" match {
        case JArray(apps) =>
          val appInfoBuffer = new ArrayBuffer[YarnAppInfo]()
          apps.foreach { app =>
            val yarnQueueName = (app \ "queue").asInstanceOf[JString].values
            val state = (app \ "state").asInstanceOf[JString].values
            if (yarnQueueName == realQueueName && (state == "RUNNING" || state == "ACCEPTED")) {
              val appInfo = new YarnAppInfo(
                (app \ "id").asInstanceOf[JString].values,
                (app \ "user").asInstanceOf[JString].values,
                state,
                (app \ "applicationType").asInstanceOf[JString].values,
                getYarnResource(Some(app)).get
              )
              appInfoBuffer.append(appInfo)
            }
          }
          appInfoBuffer.toArray
        case _ => new ArrayBuffer[YarnAppInfo](0).toArray
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
    val authorEnable: Any = this.provider.getConfigMap.get("authorEnable");
    var httpResponse: HttpResponse = null
    authorEnable match {
      case flag: Boolean =>
        if (flag) {
          httpGet.addHeader(HttpHeaders.AUTHORIZATION, "Basic " + getAuthorizationStr)
        }
      case _ =>
    }
    val kerberosEnable: Any = this.provider.getConfigMap.get("kerberosEnable");
    kerberosEnable match {
      case flag: Boolean =>
        if (flag) {
          val principalName = this.provider.getConfigMap.get("principalName").asInstanceOf[String]
          val keytabPath = this.provider.getConfigMap.get("keytabPath").asInstanceOf[String]
          val krb5Path = this.provider.getConfigMap.get("krb5Path").asInstanceOf[String]
          val requestKuu = new RequestKerberosUrlUtils(principalName, keytabPath, krb5Path, false)
          val response =
            requestKuu.callRestUrl(rmWebAddress + "/ws/v1/cluster/" + url, principalName)
          httpResponse = response;
        } else {
          val response = YarnResourceRequester.httpClient.execute(httpGet)
          httpResponse = response
        }
      case _ =>
        val response = YarnResourceRequester.httpClient.execute(httpGet)
        httpResponse = response
    }
    parse(EntityUtils.toString(httpResponse.getEntity()))
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
                response \ "clusterInfo" \ "haState" match {
                  case state: JString =>
                    if (HASTATE_ACTIVE.equalsIgnoreCase(state.s)) {
                      activeAddress = address
                    } else {
                      logger.warn(s"Resourcemanager : ${address} haState : ${state.s}")
                    }
                  case _ =>
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
