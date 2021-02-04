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

import java.util

import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.ENGINE_CREATE_MAX_WAIT_TIME
import com.webank.wedatasphere.linkis.entrance.execute.{EngineRequester, EntranceJob}
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEngineRequesterBeanAnnotation
import com.webank.wedatasphere.linkis.entranceclient.context.ClientTask
import com.webank.wedatasphere.linkis.entranceclient.execute.ClientEngineRequester
import com.webank.wedatasphere.linkis.protocol.engine.{RequestEngine, TimeoutRequestNewEngine}
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.scheduler.queue.Job
import com.webank.wedatasphere.linkis.storage.domain.MethodEntitySerializer
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}
import javax.annotation.PostConstruct
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
/**
  * Created by johnnwang on 2018/11/1.
  */
@ClientEngineRequesterBeanAnnotation
class IOEngineRequester extends ClientEngineRequester{
  @PostConstruct
  def init():Unit = {
    info("io engine requester init")
  }
  override protected def createRequestEngine(job: Job): RequestEngine = job match {
    case entranceJob: EntranceJob =>{
      val code = entranceJob.getTask match {
        case task: RequestPersistTask => task.getCode
        case task: ClientTask => task.getCode
        case _ => null
      }
      val requestEngine = super.createRequestEngine(job)
      val map = requestEngine.properties
      if(code != null){
        val method = MethodEntitySerializer.deserializer(code)
        map(StorageConfiguration.IO_USER.key) = if(method.fsType == StorageUtils.HDFS) StorageConfiguration.HDFS_ROOT_USER.getValue else StorageConfiguration.LOCAL_ROOT_USER.getValue
      }
      TimeoutRequestNewEngine(ENGINE_CREATE_MAX_WAIT_TIME.getValue.toLong, requestEngine.user, requestEngine.creator, map)
    }
  }
}
