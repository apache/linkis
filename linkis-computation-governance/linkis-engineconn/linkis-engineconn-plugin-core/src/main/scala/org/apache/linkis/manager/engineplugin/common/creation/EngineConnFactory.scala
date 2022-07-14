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
 
package org.apache.linkis.manager.engineplugin.common.creation


import java.util

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.engineconn.common.creation.EngineCreationContext
import org.apache.linkis.engineconn.common.engineconn.{DefaultEngineConn, EngineConn}
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.EngineConnModeLabel
import org.apache.linkis.manager.label.entity.engine.EngineType.EngineType

import scala.collection.JavaConversions.asScalaBuffer


trait EngineConnFactory {

  def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn

}

trait AbstractEngineConnFactory extends EngineConnFactory {

  protected def getEngineConnType: EngineType

  protected def createEngineConnSession(engineCreationContext: EngineCreationContext): Any

  override def createEngineConn(engineCreationContext: EngineCreationContext): EngineConn = {
    val engineConn = new DefaultEngineConn(engineCreationContext)
    val engineConnSession = createEngineConnSession(engineCreationContext)
    engineConn.setEngineConnType(getEngineConnType.toString)
    engineConn.setEngineConnSession(engineConnSession)
    engineConn
  }
}

/**
  * For only one kind of executor, like hive, python ...
  */
trait SingleExecutorEngineConnFactory extends AbstractEngineConnFactory with ExecutorFactory

trait SingleLabelExecutorEngineConnFactory extends SingleExecutorEngineConnFactory with LabelExecutorFactory

/**
  * For many kinds of executor, such as spark with spark-sql and spark-shell and pyspark
  */
trait MultiExecutorEngineConnFactory extends AbstractEngineConnFactory with Logging {


  def getExecutorFactories: Array[ExecutorFactory]

  def getDefaultExecutorFactory: ExecutorFactory =
    getExecutorFactories.find(_.getClass == getDefaultExecutorFactoryClass)
      .getOrElse(throw new EngineConnBuildFailedException(20000, "Cannot find default ExecutorFactory."))

  protected def getDefaultExecutorFactoryClass: Class[_ <: ExecutorFactory]

  protected def getEngineConnModeLabel(labels: util.List[Label[_]]): EngineConnModeLabel =
    labels.find(_.isInstanceOf[EngineConnModeLabel]).map(_.asInstanceOf[EngineConnModeLabel]).orNull

}