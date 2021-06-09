package com.webank.wedatasphere.linkis.engineconn.core.execution

import com.webank.wedatasphere.linkis.common.utils.{ClassUtils, Logging, Utils}
import com.webank.wedatasphere.linkis.engineconn.common.execution.EngineConnExecution
import com.webank.wedatasphere.linkis.manager.engineplugin.common.exception.EngineConnBuildFailedException

import scala.collection.convert.decorateAsScala._


object EngineConnExecution extends Logging {

  private val engineExecutions = initEngineExecutions.sortBy(_.getOrder)

  private def initEngineExecutions: Array[EngineConnExecution] = {
    Utils.tryThrow {
      val reflections = ClassUtils.reflections
      val allSubClass = reflections.getSubTypesOf(classOf[EngineConnExecution])
      allSubClass.asScala.filter(! ClassUtils.isInterfaceOrAbstract(_)).map(_.newInstance).toArray
    }(t => throw new EngineConnBuildFailedException(20000, "Cannot instance EngineConnExecution.", t))
  }

  def getEngineConnExecutions: Array[EngineConnExecution] = engineExecutions

}