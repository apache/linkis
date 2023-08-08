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

package org.apache.linkis.engineconnplugin.flink.listener

import java.util

import scala.collection.JavaConverters._

trait FlinkListenerGroup {

  def addFlinkListener(flinkListener: FlinkListener): Unit

  def getFlinkListeners: util.List[FlinkListener]

  def setFlinkListeners(flinkListeners: util.List[FlinkListener]): Unit

}

abstract class FlinkListenerGroupImpl extends FlinkListenerGroup {

  private var flinkListeners: util.List[FlinkListener] = _

  override def addFlinkListener(flinkListener: FlinkListener): Unit = {
    if (flinkListeners == null) flinkListeners = new util.ArrayList[FlinkListener]
    flinkListeners.add(flinkListener)
  }

  override def getFlinkListeners: util.List[FlinkListener] = flinkListeners

  override def setFlinkListeners(flinkListeners: util.List[FlinkListener]): Unit = {
    this.flinkListeners = flinkListeners
  }

  private def getFlinkListeners[T <: FlinkListener](clazz: Class[T]): util.List[T] =
    flinkListeners match {
      case listeners: util.List[FlinkListener] =>
        listeners.asScala.filter(clazz.isInstance).map(_.asInstanceOf[T]).asJava
      case _ => new util.ArrayList[T]
    }

  def getFlinkStatusListeners: util.List[FlinkStatusListener] = getFlinkListeners(
    classOf[FlinkStatusListener]
  )

  def getFlinkStreamingResultSetListeners: util.List[FlinkStreamingResultSetListener] =
    getFlinkListeners(classOf[FlinkStreamingResultSetListener])

}
