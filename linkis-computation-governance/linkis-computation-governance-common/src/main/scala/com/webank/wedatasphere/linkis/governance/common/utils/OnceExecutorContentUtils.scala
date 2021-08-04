package com.webank.wedatasphere.linkis.governance.common.utils

import com.webank.wedatasphere.linkis.governance.common.entity.job.OnceExecutorContent
import com.webank.wedatasphere.linkis.governance.common.exception.GovernanceErrorException
import java.util

import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant


object OnceExecutorContentUtils {

  val ONCE_EXECUTOR_CONTENT_KEY = "onceExecutorContent"
  private val HEADER = "resource_"
  private val LEN = 3

  def resourceToValue(bmlResource: BmlResource): String = {
    val resourceId = bmlResource.getResourceId
    val resourceIdLength = resourceId.length.toString.length
    if(resourceIdLength > LEN) throw new GovernanceErrorException(40108, s"Invalid resourceId $resourceId, it is too length.")
    val len = if(resourceIdLength < LEN) "0" * (LEN - resourceIdLength) + resourceId.length
    HEADER + len + resourceId + bmlResource.getVersion
  }

  def valueToResource(resource: String): BmlResource = {
    if(!resource.startsWith(HEADER)) throw new GovernanceErrorException(40108, s"Invalid resource $resource, it doesn't contain $HEADER.")
    val value = resource.substring(HEADER.length)
    val len = value.substring(0, LEN)
    val resourceId = value.substring(LEN, len.toInt + LEN)
    val version = value.substring(len.toInt + LEN)
    BmlResource(resourceId, version)
  }

  def mapToContent(contentMap: util.Map[String, Object]): OnceExecutorContent = {
    val onceExecutorContent = new OnceExecutorContent
    implicit def getOrNull(key: String): util.Map[String, Object] = contentMap.get(key) match {
      case map: util.Map[String, Object] => map
      case _ => null
    }
    onceExecutorContent.setJobContent(TaskConstant.JOB_CONTENT)
    onceExecutorContent.setRuntimeMap(TaskConstant.PARAMS_CONFIGURATION_RUNTIME)
    onceExecutorContent.setSourceMap(TaskConstant.SOURCE)
    onceExecutorContent.setVariableMap(TaskConstant.PARAMS_VARIABLE)
    onceExecutorContent
  }

  def contentToMap(onceExecutorContent: OnceExecutorContent): util.Map[String, Object] = {
    val contentMap = new util.HashMap[String, Object]()
    contentMap.put(TaskConstant.JOB_CONTENT, onceExecutorContent.getJobContent)
    contentMap.put(TaskConstant.PARAMS_CONFIGURATION_RUNTIME, onceExecutorContent.getRuntimeMap)
    contentMap.put(TaskConstant.SOURCE, onceExecutorContent.getSourceMap)
    contentMap.put(TaskConstant.PARAMS_VARIABLE, onceExecutorContent.getVariableMap)
    contentMap
  }

  trait BmlResource {
    def getResourceId: String
    def getVersion: String
    override def toString: String = s"BmlResource($getResourceId, $getVersion)"
  }

  object BmlResource {
    def apply(resourceId: String, version: String): BmlResource = new BmlResource {
      override def getResourceId: String = resourceId
      override def getVersion: String = version
    }
  }

}