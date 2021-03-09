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

package com.webank.wedatasphere.linkis.entrance.io

import java.lang.reflect.UndeclaredThrowableException
import java.util

import com.webank.wedatasphere.linkis.entranceclient.EntranceClient
import com.webank.wedatasphere.linkis.entranceclient.annotation.DefaultEntranceClientBeanAnnotation
import com.webank.wedatasphere.linkis.entranceclient.execute.ClientJob
import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.entrance.EntranceServer
import com.webank.wedatasphere.linkis.entrance.exception.EntranceErrorException
import com.webank.wedatasphere.linkis.entrance.execute.FixedInstanceEntranceExecutorRuler
import com.webank.wedatasphere.linkis.io.utils.JobUtils
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.rpc.exception.NoInstanceExistsException
import com.webank.wedatasphere.linkis.rpc.utils.RPCUtils
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEventState
import com.webank.wedatasphere.linkis.storage.domain.{MethodEntity, MethodEntitySerializer}
import com.webank.wedatasphere.linkis.storage.exception.{FSNotInitException, StorageErrorException}
import com.webank.wedatasphere.linkis.storage.io.IOClient
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}
import javax.annotation.PostConstruct
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
  * Created by johnnwang on 2018/11/1.
  */

@Component("ioClient")
class DefaultIOClient extends IOClient with Logging{

  @DefaultEntranceClientBeanAnnotation.DefaultEntranceClientAutowiredAnnotation
  private var defaultClient: EntranceClient = _

  @PostConstruct
  def init(): Unit = {
    IOClient.register(this)
  }

  def getRes(resultSets: Array[String]): String = {
    if (resultSets == null || resultSets.isEmpty)
      IOClient.SUCCESS
    else
      resultSets(0)
  }



  override def execute(user: String, methodEntity: MethodEntity, params: java.util.Map[String, Any]): String = {
    if (params.containsKey(StorageUtils.FIXED_INSTANCE) && StringUtils.isNotEmpty(params.get(StorageUtils.FIXED_INSTANCE).toString)) {
      val instance = params.get(StorageUtils.FIXED_INSTANCE).toString
      val specialMap = TaskUtils.getSpecialMap(params)
      specialMap.put(FixedInstanceEntranceExecutorRuler.FIXED_INSTANCE, instance)
      TaskUtils.addSpecialMap(params, specialMap)
      Utils.tryCatch{
        val clientJob =
        if("exists".equals(methodEntity.methodName)){
          executeResult(user,methodEntity,params,1)
        } else executeResult(user,methodEntity,params)
        getRes(clientJob.getResultSets)
      }{ t: Throwable =>
        if (RPCUtils.isReceiverNotExists(t))
          throw new FSNotInitException()
        else
          throw t
      }
    } else {
      throw new FSNotInitException()
    }
  }

  def executeResult(user:String,methodEntity: MethodEntity, params: java.util.Map[String, Any],reTryLimit:Int = 0):ClientJob = {
    val creator = StorageConfiguration.IO_DEFAULT_CREATOR.getValue
    val code = MethodEntitySerializer.serializer(methodEntity)
    params.put(EntranceServer.DO_NOT_PRINT_PARAMS_LOG, true)
    var clientJob = defaultClient.getJob(defaultClient.executeJob(code, user, creator, params)).get
    clientJob.waitForComplete()
    var initCount = 0
    while (!clientJob.isSucceed && initCount < reTryLimit ){
      val engine = params.getOrDefault(FixedInstanceEntranceExecutorRuler.FIXED_INSTANCE,"null")
      info(s"jobId(${clientJob.getId}) For user:$user  to retry($initCount) Fixed Engine($engine),code:$methodEntity ")
      clientJob = defaultClient.getJob(defaultClient.executeJob(code, user, creator, params)).get
      clientJob.waitForComplete()
      initCount = initCount + 1
    }
    if(clientJob.isSucceed){
      val resultSets = clientJob.getResultSets
      if(resultSets == null || resultSets.isEmpty){
        info(s"jobId(${clientJob.getId}) execute method get res is null,$methodEntity")
      }
     clientJob
    }else {
      if(clientJob.getErrorResponse != null && clientJob.getErrorResponse.t != null){
        info(s"jobId(${clientJob.getId}) Failed to execute method fs for user:$user by retry:$initCount,code:$methodEntity reason:${clientJob.getErrorResponse.message}")
        clientJob.getErrorResponse.t match {
          case e:UndeclaredThrowableException => if(e.getUndeclaredThrowable.isInstanceOf[NoInstanceExistsException]){
            throw new FSNotInitException()
          }
          case e:EntranceErrorException => if(28001 == e.getErrCode){
            throw new FSNotInitException()
          }
        }
      }
      throw new StorageErrorException(52005,s"jobId(${clientJob.getId}) Failed to execute method fs for user:$user by retry:$initCount,code:$methodEntity")
    }
  }

  override def executeWithEngine(user: String, methodEntity: MethodEntity, params: util.Map[String, Any]): Array[String] = {
    val reTryLimit = StorageConfiguration.IO_INIT_RETRY_LIMIT.getValue
    val clientJob = executeResult(user,methodEntity,params,reTryLimit)
    if(clientJob.isSucceed){
      val res = getRes(clientJob.getResultSets)
      val instance = JobUtils.getEngineInstance(clientJob)
      Array[String](res, instance)
    } else {
      throw new StorageErrorException(52005,s"jobId(${clientJob.getId})Failed to init fs for user:$user by retry:$reTryLimit")
    }
  }
}
