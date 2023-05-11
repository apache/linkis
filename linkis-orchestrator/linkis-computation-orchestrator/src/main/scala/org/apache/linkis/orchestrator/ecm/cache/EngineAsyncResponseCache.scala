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

package org.apache.linkis.orchestrator.ecm.cache

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.manager.common.protocol.RequestManagerUnlock
import org.apache.linkis.manager.common.protocol.engine.{
  EngineAsyncResponse,
  EngineCreateError,
  EngineCreateSuccess
}
import org.apache.linkis.orchestrator.ecm.conf.ECMPluginConf
import org.apache.linkis.orchestrator.ecm.exception.ECMPluginCacheException
import org.apache.linkis.rpc.Sender

import org.apache.commons.lang3.exception.ExceptionUtils

import java.util.concurrent.{TimeoutException, TimeUnit}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration

/**
 */
trait EngineAsyncResponseCache {

  @throws[ECMPluginCacheException]
  def put(id: String, engineAsyncResponse: EngineAsyncResponse): Unit

  def get(id: String, timeout: Duration): EngineAsyncResponse

  def getAndRemove(id: String, timeout: Duration): EngineAsyncResponse

}

object EngineAsyncResponseCache {

  private val engineAsyncResponseCache: EngineAsyncResponseCache = new EngineAsyncResponseCacheMap

  def getCache: EngineAsyncResponseCache = engineAsyncResponseCache

}

case class EngineAsyncResponseEntity(engineAsyncResponse: EngineAsyncResponse, createTime: Long)

class EngineAsyncResponseCacheMap extends EngineAsyncResponseCache with Logging {

  private val cacheMap: java.util.Map[String, EngineAsyncResponseEntity] =
    new java.util.concurrent.ConcurrentHashMap[String, EngineAsyncResponseEntity]()

  private val expireTime = ECMPluginConf.EC_ASYNC_RESPONSE_CLEAR_TIME.getValue.toLong

  init()

  override def get(id: String, timeout: Duration): EngineAsyncResponse = {
    Utils.waitUntil(() => cacheMap.containsKey(id), timeout)
    val engineAsyncResponseEntity = cacheMap.get(id)
    if (null != engineAsyncResponseEntity) {
      engineAsyncResponseEntity.engineAsyncResponse
    } else {
      new EngineCreateError(id, "async info null", true)
    }
  }

  override def getAndRemove(id: String, timeout: Duration): EngineAsyncResponse = {
    Utils.tryCatch {
      Utils.waitUntil(() => cacheMap.containsKey(id), timeout)
    } {
      case t: TimeoutException =>
        put(
          id,
          new EngineCreateError(
            id,
            s"Asynchronous request engine timeout(请求引擎超时，可能是因为资源不足，您可以选择重试),async id $id",
            true
          )
        )
      case t: Throwable =>
        put(id, new EngineCreateError(id, ExceptionUtils.getRootCauseStackTrace(t).mkString("\n")))
    }
    val engineAsyncResponseEntity = cacheMap.remove(id)
    if (null != engineAsyncResponseEntity) {
      engineAsyncResponseEntity.engineAsyncResponse
    } else {
      new EngineCreateError(id, "async info null", true)
    }
  }

  @throws[ECMPluginCacheException]
  override def put(id: String, engineAsyncResponse: EngineAsyncResponse): Unit = {
    if (cacheMap.containsKey(id)) {
      cacheMap.remove(id)
      throw new ECMPluginCacheException(ECMPluginConf.ECM_CACHE_ERROR_CODE, "id duplicate")
    }
    cacheMap.put(id, EngineAsyncResponseEntity(engineAsyncResponse, System.currentTimeMillis()))
  }

  def init(): Unit = {
    logger.info(s"Start cache map clear defaultScheduler")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = try {

          val iterator = cacheMap.entrySet().iterator()
          val expireBuffer = new ArrayBuffer[String]()
          while (iterator.hasNext) {
            val keyValue = iterator.next()
            val curTime = System.currentTimeMillis() - expireTime
            if (null != keyValue.getValue && keyValue.getValue.createTime < curTime) {
              expireBuffer += keyValue.getKey
            }
          }
          expireBuffer.foreach { key =>
            logger.info(s" to clear engineAsyncResponseEntity key $key")
            val engineAsyncResponseEntity = cacheMap.remove(key)
            if (
                null != engineAsyncResponseEntity && engineAsyncResponseEntity.engineAsyncResponse
                  .isInstanceOf[EngineCreateSuccess]
            ) {
              val engineCreateSuccess =
                engineAsyncResponseEntity.engineAsyncResponse.asInstanceOf[EngineCreateSuccess]
              logger.info(s"clear engineCreateSuccess, to unlock $engineCreateSuccess")
              val requestManagerUnlock = new RequestManagerUnlock(
                engineCreateSuccess.getEngineNode.getServiceInstance,
                engineCreateSuccess.getEngineNode.getLock,
                Sender.getThisServiceInstance
              )
              getManagerSender.send(requestManagerUnlock)
            }
          }
        } catch {
          case throwable: Throwable =>
            logger.error("Failed to clear EngineAsyncResponseCacheMap", throwable)
        }
      },
      60000,
      expireTime,
      TimeUnit.MILLISECONDS
    )
  }

  private def getManagerSender: Sender =
    Sender.getSender(GovernanceCommonConf.MANAGER_SERVICE_NAME.getValue)

}
