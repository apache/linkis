/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.manager.engineplugin.common.creation


import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext
import com.webank.wedatasphere.linkis.engineconn.common.engineconn.EngineConn
import com.webank.wedatasphere.linkis.engineconn.executor.entity.Executor
import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineRunTypeLabel
import org.reflections.Reflections

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._


trait EngineConnFactory {

  def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn

}

/**
  * For only one kind of executor, like hive, python ...
  */
trait SingleExecutorEngineConnFactory extends EngineConnFactory {

  def createExecutor(engineCreationContext: EngineCreationContext, engineConn: EngineConn): Executor

  def getDefaultEngineRunTypeLabel(): EngineRunTypeLabel
}

/**
  * For many kinds of executor, such as spark with spark-sql and spark-shell and pyspark
  */
trait MultiExecutorEngineConnFactory extends EngineConnFactory with Logging {

  def getExecutorFactories: Array[ExecutorFactory] = {
    val executorFactories = new ArrayBuffer[ExecutorFactory]
    Utils.tryCatch {
      val reflections = new Reflections("com.webank.wedatasphere.linkis", classOf[ExecutorFactory])
      val allSubClass = reflections.getSubTypesOf(classOf[ExecutorFactory])
      allSubClass.asScala.foreach(l => {
        executorFactories += l.newInstance
      })
    } {
      t: Throwable =>
        error(t.getMessage)
    }
    executorFactories.toArray
  }

  def getDefaultExecutorFactory: ExecutorFactory = {
    var defaultExecutorFactory: ExecutorFactory = null
    getExecutorFactories.foreach(f => {
      if (null == defaultExecutorFactory) {
        defaultExecutorFactory = f
      } else if (f.getOrder < defaultExecutorFactory.getOrder) {
          defaultExecutorFactory = f
        }
    })
    defaultExecutorFactory
  }

}