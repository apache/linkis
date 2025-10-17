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

package org.apache.linkis.manager.engineplugin.io.executor

import org.apache.linkis.common.io.{Fs, FsPath}
import org.apache.linkis.common.utils.{Logging, OverloadUtils, Utils}
import org.apache.linkis.engineconn.computation.executor.execute.{
  ConcurrentComputationExecutor,
  EngineExecutionContext
}
import org.apache.linkis.engineconn.core.EngineConnObject
import org.apache.linkis.manager.common.entity.resource.{
  CommonNodeResource,
  LoadResource,
  NodeResource
}
import org.apache.linkis.manager.engineplugin.common.util.NodeResourceUtils
import org.apache.linkis.manager.engineplugin.io.conf.IOEngineConnConfiguration
import org.apache.linkis.manager.engineplugin.io.domain.FSInfo
import org.apache.linkis.manager.engineplugin.io.service.FsProxyService
import org.apache.linkis.manager.engineplugin.io.utils.{IOHelp, ReflectionUtils}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.{
  AliasOutputExecuteResponse,
  ExecuteResponse,
  SuccessExecuteResponse
}
import org.apache.linkis.storage.FSFactory
import org.apache.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import org.apache.linkis.storage.errorcode.LinkisIoFileErrorCodeSummary.{
  FS_CAN_NOT_PROXY_TO,
  NOT_EXISTS_METHOD,
  PARAMETER_CALLS
}
import org.apache.linkis.storage.exception.{StorageErrorCode, StorageErrorException}
import org.apache.linkis.storage.fs.FileSystem
import org.apache.linkis.storage.utils.StorageUtils

import org.apache.commons.io.IOUtils

import java.util
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import com.google.gson.internal.LinkedTreeMap

class IoEngineConnExecutor(val id: Int, val outputLimit: Int = 10)
    extends ConcurrentComputationExecutor(outputLimit)
    with Logging {

  val fsIdCount = new AtomicLong()

  val FS_ID_LIMIT = IOEngineConnConfiguration.IO_FS_ID_LIMIT.getValue
  // The key is the user, and the value is the FS array applied by the user
  private val userFSInfos = new util.HashMap[String, ArrayBuffer[FSInfo]]()

  private val fsProxyService = new FsProxyService

  private var initialized: Boolean = _

  var executorLabels: util.List[Label[_]] = new util.ArrayList[Label[_]](2)

  private val namePrefix: String = "IoEngineConnExecutor_"

  override def init(): Unit = {
    super.init
    logger.info("Ready to start IoEngine!")
    cleanupThread.start()
  }

  /*
   * Regularly clean up idle FS
   */
  private val cleanupThread = new Thread("IOEngineExecutor-Cleanup-Scanner") {
    setDaemon(true)
    var continue = true

    override def run(): Unit = {
      while (continue) {
        var clearCount = 0
        userFSInfos.asScala.filter(_._2.exists(_.timeout)).foreach { case (_, list) =>
          list synchronized list.filter(_.timeout).foreach { s =>
            s.fs.close()
            list -= s
            clearCount = clearCount + 1
          }
        }
        logger.debug(s"Finished to clear userFs, clear count: $clearCount")
        Utils.tryQuietly(Thread.sleep(IOEngineConnConfiguration.IO_FS_CLEAR_TIME.getValue))
      }
    }

  }

  override def executeLine(
      engineExecutionContext: EngineExecutionContext,
      code: String
  ): ExecuteResponse = {
    val method = MethodEntitySerializer.deserializer(code)
    val methodName = method.methodName
    val jobID = engineExecutionContext.getJobId.get
    logger.info(
      s"jobID($jobID):creator ${method.creatorUser} proxy user: ${method.proxyUser} to execute a method: ${method.methodName}.,fsId=${method.id}"
    )
    val executeResponse = methodName match {
      case "init" =>
        val fsId: Long = if (!existsUserFS(method)) {
          createUserFS(method)
        } else {
          method.id
        }
        logger.info(s"jobID($jobID),user(${method.proxyUser}) execute init and fsID($fsId)")
        AliasOutputExecuteResponse(
          fsId.toString,
          StorageUtils.serializerStringToResult(fsId.toString)
        )
      case "close" => closeUserFS(method); SuccessExecuteResponse()
      case "read" =>
        val fs = getUserFS(method)
        AliasOutputExecuteResponse(method.id.toString, IOHelp.read(fs, method))
      case "available" =>
        val fs = getUserFS(method)
        if (method.params == null || method.params.length != 2) {
          throw new StorageErrorException(
            PARAMETER_CALLS.getErrorCode,
            PARAMETER_CALLS.getErrorDesc
          )
        }
        val dest = MethodEntitySerializer.deserializerToJavaObject(
          method.params(0).asInstanceOf[String],
          classOf[FsPath]
        )
        val position =
          if (method.params(1).toString.toInt < 0) 0 else method.params(1).toString.toInt
        val inputStream = fs.read(dest)
        Utils.tryFinally(
          AliasOutputExecuteResponse(
            method.id.toString,
            StorageUtils.serializerStringToResult((inputStream.available() - position).toString)
          )
        )(IOUtils.closeQuietly(inputStream))
      case "write" =>
        val fs = getUserFS(method)
        IOHelp.write(fs, method)
        SuccessExecuteResponse()
      case "renameTo" =>
        val fs = getUserFS(method)
        if (method.params == null || method.params.length != 2) {
          throw new StorageErrorException(
            PARAMETER_CALLS.getErrorCode,
            PARAMETER_CALLS.getErrorDesc
          )
        }
        fs.renameTo(
          MethodEntitySerializer
            .deserializerToJavaObject(method.params(0).asInstanceOf[String], classOf[FsPath]),
          MethodEntitySerializer.deserializerToJavaObject(
            method.params(1).asInstanceOf[String],
            classOf[FsPath]
          )
        )
        SuccessExecuteResponse()
      case "list" =>
        if (method.params == null || method.params.length != 1) {
          throw new StorageErrorException(
            PARAMETER_CALLS.getErrorCode,
            PARAMETER_CALLS.getErrorDesc
          )
        }
        val fs = getUserFS(method)
        val dest = MethodEntitySerializer.deserializerToJavaObject(
          method.params(0).asInstanceOf[String],
          classOf[FsPath]
        )
        AliasOutputExecuteResponse(
          method.id.toString,
          StorageUtils.serializerStringToResult(
            MethodEntitySerializer.serializerJavaObject(fs.list(dest))
          )
        )
      case "listPathWithError" =>
        if (method.params == null || method.params.length != 1) {
          throw new StorageErrorException(
            PARAMETER_CALLS.getErrorCode,
            PARAMETER_CALLS.getErrorDesc
          )
        }
        val fs = getUserFS(method).asInstanceOf[FileSystem]
        val dest = MethodEntitySerializer.deserializerToJavaObject(
          method.params(0).asInstanceOf[String],
          classOf[FsPath]
        )
        AliasOutputExecuteResponse(
          method.id.toString,
          StorageUtils.serializerStringToResult(
            MethodEntitySerializer.serializerJavaObject(fs.listPathWithError(dest))
          )
        )
      case "get" | "create" | "isOwner" =>
        invokeMethod(method, classOf[String], jobID)
      case _ =>
        invokeMethod(method, classOf[FsPath], jobID)
    }
    logger.info(
      s"jobID($jobID):creator ${method.creatorUser} proxy user: ${method.proxyUser} finished to  execute a method: ${method.methodName}.,fsId=${method.id}"
    )
    executeResponse
  }

  override def executeCompletely(
      engineExecutorContext: EngineExecutionContext,
      code: String,
      completedLine: String
  ): ExecuteResponse = null

  override def progress(taskID: String): Float = 1.0f

  override def getProgressInfo(taskID: String): Array[JobProgressInfo] = null

  override def supportCallBackLogs(): Boolean = false

  override def requestExpectedResource(expectedResource: NodeResource): NodeResource = null

  override def getCurrentNodeResource(): NodeResource = {
    NodeResourceUtils.appendMemoryUnitIfMissing(
      EngineConnObject.getEngineCreationContext.getOptions
    )

    val resource = new CommonNodeResource
    val usedResource = new LoadResource(OverloadUtils.getProcessMaxMemory, 1)
    resource.setUsedResource(usedResource)
    resource
  }

  override def getExecutorLabels(): util.List[Label[_]] = executorLabels

  override def setExecutorLabels(labels: util.List[Label[_]]): Unit = {
    if (null != labels && !labels.isEmpty) {
      executorLabels.clear()
      executorLabels.addAll(labels)
    }
  }

  override def getId(): String = namePrefix + id

  def getFSId(): Long = {
    if (fsIdCount.get() == FS_ID_LIMIT) {
      fsIdCount.getAndSet(0)
    } else {
      fsIdCount.getAndIncrement()
    }
  }

  private def existsUserFS(methodEntity: MethodEntity): Boolean = {
    val proxyUser = methodEntity.proxyUser
    if (!userFSInfos.containsKey(proxyUser)) return false
    userFSInfos.get(proxyUser).synchronized {
      val userFsInfo =
        userFSInfos.get(proxyUser).find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
      userFsInfo.foreach(_.lastAccessTime = System.currentTimeMillis())
      userFsInfo.isDefined
    }
  }

  protected def getUserFS(methodEntity: MethodEntity): Fs = {
    val fsType = methodEntity.fsType
    val proxyUser = methodEntity.proxyUser
    if (!userFSInfos.containsKey(proxyUser)) {
      if (methodEntity.id != -1) {
        createUserFS(methodEntity)
      } else {
        throw new StorageErrorException(
          StorageErrorCode.FS_NOT_INIT.getCode,
          s"not exist storage $fsType, ${StorageErrorCode.FS_NOT_INIT.getMessage}"
        )
      }
    }
    var fs: Fs = null
    userFSInfos.get(proxyUser) synchronized {
      val userFsInfoOption = userFSInfos
        .get(proxyUser)
        .find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
      if (userFsInfoOption.isDefined) {
        val userFsInfo = userFsInfoOption.get
        userFsInfo.lastAccessTime = System.currentTimeMillis()
        fs = userFsInfo.fs
      }
    }
    if (null == fs) {
      if (methodEntity.id != -1) {
        createUserFS(methodEntity)
        getUserFS(methodEntity)
      } else {
        throw new StorageErrorException(
          StorageErrorCode.FS_NOT_INIT.getCode,
          s"not exist storage $fsType, ${StorageErrorCode.FS_NOT_INIT.getMessage}"
        )
      }
    } else {
      fs
    }
  }

  private def createUserFS(methodEntity: MethodEntity): Long = {
    logger.info(
      s"Creator ${methodEntity.creatorUser} for user ${methodEntity.proxyUser} init fs $methodEntity"
    )
    var fsId = methodEntity.id
    val properties =
      methodEntity.params(0).asInstanceOf[LinkedTreeMap[String, String]].asScala.toMap
    val proxyUser = methodEntity.proxyUser
    if (!fsProxyService.canProxyUser(methodEntity.creatorUser, proxyUser, methodEntity.fsType)) {
      throw new StorageErrorException(
        FS_CAN_NOT_PROXY_TO.getErrorCode,
        s"FS Can not proxy to：$proxyUser"
      )
    }
    if (!userFSInfos.containsKey(proxyUser)) {
      userFSInfos synchronized {
        if (!userFSInfos.containsKey(proxyUser)) {
          userFSInfos.put(proxyUser, ArrayBuffer[FSInfo]())
        }
      }
    }
    val userFsInfo = userFSInfos.get(proxyUser)
    userFsInfo synchronized {
      if (!userFsInfo.exists(fsInfo => fsInfo != null && fsInfo.id == fsId)) {
        val fs = FSFactory.getFs(methodEntity.fsType)
        fs.init(properties.asJava)
        fsId = if (fsId == -1) getFSId() else fsId
        userFsInfo += new FSInfo(fsId, fs)
      }
    }
    logger.info(
      s"Creator ${methodEntity.creatorUser} for user ${methodEntity.proxyUser} end init fs fsId=$fsId"
    )
    fsId
  }

  private def closeUserFS(methodEntity: MethodEntity): Unit = {
    logger.info(
      s"Creator ${methodEntity.creatorUser} for user ${methodEntity.proxyUser} close FS：$methodEntity"
    )
    val proxyUser = methodEntity.proxyUser
    if (!userFSInfos.containsKey(proxyUser)) return
    val userFsInfo = userFSInfos.get(proxyUser)
    userFsInfo synchronized {
      val fsInfo = userFsInfo.find(fsInfo => fsInfo != null && fsInfo.id == methodEntity.id)
      if (fsInfo.isDefined) {
        Utils.tryFinally(fsInfo.get.fs.close()) {
          userFsInfo -= fsInfo.get
          if (userFsInfo.isEmpty) {
            logger.info(s"Prepare to clear userFsInfo:$methodEntity")
            userFSInfos.remove(proxyUser)
          }
        }
      }
    }
  }

  private def invokeMethod[T](
      method: MethodEntity,
      methodParamType: Class[T],
      jobID: String
  ): AliasOutputExecuteResponse = {
    val fs = getUserFS(method)
    val methodName = method.methodName
    val parameterSize = if (method.params == null) 0 else method.params.length
    val realMethod = fs.getClass.getMethods
      .filter(_.getName == methodName)
      .find(_.getGenericParameterTypes.length == parameterSize)
    if (realMethod.isEmpty) {
      throw new StorageErrorException(
        NOT_EXISTS_METHOD.getErrorCode,
        s"not exists method $methodName in fs ${fs.getClass.getSimpleName}."
      )
    }
    if (parameterSize > 0) {
      method.params(0) = MethodEntitySerializer.deserializerToJavaObject(
        method.params(0).asInstanceOf[String],
        methodParamType
      )
    }
    val res = MethodEntitySerializer.serializerJavaObject(
      ReflectionUtils.invoke(fs, realMethod.get, method.params)
    )
    if ("exists" == methodName) {
      logger.info(
        s"jobID($jobID),user(${method.proxyUser}) execute exists get res($res) and input code($method)"
      )
    }

    AliasOutputExecuteResponse(method.id.toString, StorageUtils.serializerStringToResult(res))
  }

  override def killTask(taskID: String): Unit = {
    logger.warn(s"Kill job : ${taskID}")
    super.killTask(taskID)
  }

  override def killAll(): Unit = {}
}
