/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.enginemanager

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.enginemanager.conf.EngineManagerConfiguration
import com.webank.wedatasphere.linkis.enginemanager.exception.{EMRetryException, EngineManagerErrorException, EngineManagerWarnException}
import com.webank.wedatasphere.linkis.protocol.engine.RequestEngine
import com.webank.wedatasphere.linkis.resourcemanager.{AvailableResource, NotEnoughResource, Resource}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

/**
  * Created by johnnwang on 2018/9/27.
  */
abstract class AbstractEngineManager extends EngineManager with Logging {

  private implicit val executor = AbstractEngineManager.executor
  private var engineListener: Option[EngineListener] = None

  def setEngineListener(engineListener: EngineListener) = this.engineListener = Some(engineListener)

  override def requestEngine(request: RequestEngine): Option[Engine] = requestEngine(request, 0)

  override def requestEngine(request: RequestEngine, duration: Long): Option[Engine] = {
    info("User " + request.user + " wants to request a new engine, messages: " + request)
    val startTime = System.currentTimeMillis
    var realRequest = request
    getEngineManagerContext.getOrCreateEngineHook.foreach(hook => realRequest = hook.beforeCreateSession(realRequest))
    val resource = Utils.tryThrow(getEngineManagerContext.getOrCreateEngineResourceFactory
      .createEngineResource(realRequest)){t =>
      warn(s"In the configuration of ${realRequest.creator}, there is a parameter configuration in the wrong format!(${realRequest.creator}的配置中，存在错误格式的参数配置！)", t)
      throw new EngineManagerErrorException(11011, s"In the configuration of ${realRequest.creator}, there is a parameter configuration in the wrong format!(${realRequest.creator}的配置中，存在错误格式的参数配置！)")
    }
    val nodeResourceInfo = this.registerResources()
    val usedResource = getEngineManagerContext.getOrCreateEngineFactory.getUsedResources.getOrElse(Resource.getZeroResource(resource.getResource))
    if(nodeResourceInfo.totalResource - nodeResourceInfo.protectedResource - usedResource <= resource.getResource) {
      warn("The remote server resource has been exhausted!(远程服务器资源已被用尽！)") //TODO event needed
      info("TotalResource: "+ nodeResourceInfo.totalResource.toString)
      info("ProtectedResource: "+ nodeResourceInfo.protectedResource.toString)
      info("UsedResource: "+ usedResource.toString)
      info("RequestResource: "+ resource.getResource.toString)
      throw new EngineManagerWarnException(31000, "远程服务器资源已被用光，请切换远程服务器再试！")
    }
    getEngineManagerContext.getOrCreateResourceRequester.request(resource) match {
      case NotEnoughResource(reason) =>
        throw new EngineManagerWarnException(30001, LogUtils.generateWarn(reason))
      case AvailableResource(ticketId) =>
        //The first step: get the creation request(第一步：拿到创建请求)
        val engine = getEngineManagerContext.getOrCreateEngineCreator.create(ticketId, resource, realRequest)
        engine.setResource(resource.getResource)
        def removeInInitPort(): Unit = getEngineManagerContext.getOrCreateEngineCreator match {
          case a: AbstractEngineCreator => a.removePort(engine.getPort)
        }
        engineListener.foreach(_.onEngineCreated(realRequest, engine))
        getEngineManagerContext.getOrCreateEngineFactory.addEngine(engine)
        val future = Future {
          engine.init()
          getEngineManagerContext.getOrCreateEngineHook.foreach(hook => hook.afterCreatedSession(engine, realRequest))
        }
        future onComplete  {
          case Failure(t) =>
            error(s"init ${engine.toString} failed, now stop and delete it.", t)
            removeInInitPort()
            Utils.tryAndError(getEngineManagerContext.getOrCreateEngineFactory.delete(engine))
            engineListener.foreach(_.onEngineInitFailed(engine, t))
          case Success(_) =>
            info(s"init ${engine.toString} succeed.")
            removeInInitPort()
            engineListener.foreach(_.onEngineInited(engine))
        }
        if(duration > 0 && System.currentTimeMillis - startTime < duration)
          Utils.tryQuietly(Await.result(future, Duration(System.currentTimeMillis - startTime, TimeUnit.MILLISECONDS)))
//        if(duration > 0) {
//          val leftTime = duration - System.currentTimeMillis + startTime
//          Utils.tryThrow(Await.result(future, Duration(leftTime, TimeUnit.MILLISECONDS))) {t =>
//            t match {
//              case _: TimeoutException =>
//                error(s"wait for ${engine.toString} completing initial failed, reason: sessionManager wait for too long time, killed!")
//              case _: InterruptedException =>
//                error(s"wait for ${engine.toString} completing initial failed, reason: killed by user!")
//              case _ =>
//                error(s"wait for ${engine.toString} completing initial failed!", t)
//            }
//            engine.stop()
//          }
//        }
        Some(engine)
    }
  }

}
object AbstractEngineManager {
  private val executor = Utils.newCachedExecutionContext(EngineManagerConfiguration.ENGINE_MANAGER_MAX_THREADS.getValue, "Engine-Manager-Thread-")
}