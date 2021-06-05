package com.webank.wedatasphere.linkis.governance.common.entity.job

import java.util

/**
  * Created by enjoyyin on 2021/5/21.
  */
class OnceExecutorContent {

  private var variableMap: util.Map[String, Object] = _
  private var jobContent: util.Map[String, Object] = _
  private var runtimeMap: util.Map[String, Object] = _
  private var sourceMap: util.Map[String, Object] = _
  private var extraLabels: util.Map[String, Object] = _

  def getVariableMap: util.Map[String, Object] = variableMap
  def setVariableMap(variableMap: util.Map[String, Object]): Unit = this.variableMap = variableMap

  def getJobContent: util.Map[String, Object] = jobContent
  def setJobContent(jobContent: util.Map[String, Object]): Unit = this.jobContent = jobContent

  def getRuntimeMap: util.Map[String, Object] = runtimeMap
  def setRuntimeMap(runtimeMap: util.Map[String, Object]): Unit = this.runtimeMap = runtimeMap

  def getSourceMap: util.Map[String, Object] = sourceMap
  def setSourceMap(sourceMap: util.Map[String, Object]): Unit = this.sourceMap = sourceMap

  def getExtraLabels: util.Map[String, Object] = extraLabels
  def setExtraLabels(extraLabels: util.Map[String, Object]): Unit = this.extraLabels = extraLabels

}
