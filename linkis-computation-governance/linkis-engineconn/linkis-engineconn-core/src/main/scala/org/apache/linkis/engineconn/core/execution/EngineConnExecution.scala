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

package org.apache.linkis.engineconn.core.execution

import org.apache.linkis.common.utils.{ClassUtils, Logging, Utils}
import org.apache.linkis.engineconn.common.execution.EngineConnExecution
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary.CANNOT_INSTANCE_ECE

import scala.collection.convert.decorateAsScala._

object EngineConnExecution extends Logging {

  private val engineExecutions = initEngineExecutions

  logger.info("The list of EngineConnExecution: " + engineExecutions.toList)

  private def initEngineExecutions: Array[EngineConnExecution] = {
    Utils.tryThrow {
      val reflections = ClassUtils.reflections
      val allSubClass = reflections.getSubTypesOf(classOf[EngineConnExecution])
      allSubClass.asScala
        .filter(!ClassUtils.isInterfaceOrAbstract(_))
        .map(_.newInstance)
        .toArray
        .sortBy(_.getOrder)
    }(t =>
      throw new EngineConnBuildFailedException(
        CANNOT_INSTANCE_ECE.getErrorCode,
        CANNOT_INSTANCE_ECE.getErrorDesc,
        t
      )
    )
  }

  def getEngineConnExecutions: Array[EngineConnExecution] = engineExecutions

}
