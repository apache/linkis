/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.engineconn.common.engineconn

import org.apache.linkis.engineconn.common.creation.EngineCreationContext


trait EngineConn {

  /**
    * 底层engine的类型比如：Spark、hive
    * engine conn type : spark , hive
    * @return
    */
  def getEngineConnType: String

  def setEngineConnType(engineConnType: String): Unit


  def getEngineConnSession: Any

  def setEngineConnSession(engineConnSession: Any): Unit

  def getEngineCreationContext: EngineCreationContext


}

class DefaultEngineConn(engineCreationContext: EngineCreationContext) extends EngineConn {

  private var engineConnType: String = _

  private var engineConnSession: Any = _


  /**
    * 底层engine的类型比如：Spark、hive
    *
    * @return
    */
  override def getEngineConnType: String = engineConnType

  override def setEngineConnType(engineConnType: String): Unit = this.engineConnType = engineConnType


  override def getEngineConnSession: Any = engineConnSession

  override def setEngineConnSession(engineConnSession: Any): Unit = this.engineConnSession = engineConnSession

  override def getEngineCreationContext: EngineCreationContext = engineCreationContext

}