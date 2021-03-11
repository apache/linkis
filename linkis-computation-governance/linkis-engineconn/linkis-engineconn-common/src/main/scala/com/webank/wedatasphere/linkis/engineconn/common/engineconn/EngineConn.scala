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

package com.webank.wedatasphere.linkis.engineconn.common.engineconn

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext


trait EngineConn {

  /**
    * 底层engine的类型比如：Spark、hive
    *
    * @return
    */
  def getEngineType(): String

  def setEngineType(engineType: String): Unit

  /**
    * 底层计算存储Engine的具体连接信息，比如SparkSession,hive的sessionState
    *
    * @return
    */
  def getEngine(): Any

  def setEngine(engine: Any): Unit

  def getEngineCreationContext: EngineCreationContext


}

class DefaultEngineConn(engineCreationContext: EngineCreationContext) extends EngineConn {

  var engineType: String = "spark"

  var engine: Any = null


  /**
    * 底层engine的类型比如：Spark、hive
    *
    * @return
    */
  override def getEngineType(): String = engineType

  override def setEngineType(engineType: String): Unit = this.engineType = engineType

  /**
    * 底层计算存储Engine的具体连接信息，比如SparkSession,hive的sessionState
    *
    * @return
    */
  override def getEngine(): Any = engine

  override def setEngine(engine: Any): Unit = this.engine = engine

  override def getEngineCreationContext: EngineCreationContext = engineCreationContext

}