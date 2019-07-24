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


import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEngineBuilderBeanAnnotation
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientGroupFactoryBeanAnnotation.ClientGroupFactoryAutowiredAnnotation
import com.webank.wedatasphere.linkis.entranceclient.execute.ClientEngineBuilder
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}

/**
  * Created by johnnwang on 2018/11/5.
  */
@ClientEngineBuilderBeanAnnotation
class IOEngineBuilder(@ClientGroupFactoryAutowiredAnnotation groupFactory: GroupFactory) extends ClientEngineBuilder(groupFactory) with Logging{

//  private val idGenerator = new AtomicLong(0)


  override protected def getGroupName(creator: String, user: String): String = {
    warn(s"receive a new engine for creator $user.")
    if(StorageConfiguration.HDFS_ROOT_USER.getValue == user){
      StorageUtils.HDFS
    } else {
      StorageUtils.FILE
    }
  }

//  override def buildEngine(instance: String, requestEngine: RequestEngine): EntranceEngine = {
//    warn(s"receive a new engine $instance for creator ${requestEngine.creator}")
//    val engine = createEngine(idGenerator.incrementAndGet())
//    engine.setInstance(instance)
//    val group = groupFactory.getOrCreateGroup(getGroupByName(requestEngine.properties))
//    engine.setGroup(group)
//    engine.changeState(ExecutorState.Starting, ExecutorState.Idle, null, null)
//    engine
//  }
//
//  override def buildEngine(responseEngineStatus: ResponseEngineStatus): EntranceEngine = {
//    warn(s"receive a new engine ${responseEngineStatus.instance}")
//    val engine = createEngine(idGenerator.incrementAndGet())
//    engine.setInstance(responseEngineStatus.instance)
//    val group = groupFactory.getOrCreateGroup(getGroupByName(responseEngineStatus.engineInfo.properties))
//    engine.setGroup(group)
//    engine.changeState(ExecutorState.Starting, ExecutorState.Idle, null, null)
//    engine
//  }

//  def getGroupByName(properties: java.util.Map[String,String]): String ={
//    val user = StorageConfiguration.IO_USER.getValue(properties)
//    warn(s"receive a new engine for creator $user")
//    if(StorageConfiguration.HDFS_ROOT_USER.getValue == user){
//       StorageUtils.HDFS
//    } else {
//      StorageUtils.FILE
//    }
//
//  }
}
