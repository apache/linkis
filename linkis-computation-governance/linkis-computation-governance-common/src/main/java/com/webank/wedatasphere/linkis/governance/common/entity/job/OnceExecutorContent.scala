/*
 * <!--
 *   ~ Copyright 2019 WeBank
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License.
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software
 *   ~ distributed under the License is distributed on an "AS IS" BASIS,
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   ~ See the License for the specific language governing permissions and
 *   ~ limitations under the License.
 *   -->
 *
 */

package com.webank.wedatasphere.linkis.governance.common.entity.job

import java.util


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
