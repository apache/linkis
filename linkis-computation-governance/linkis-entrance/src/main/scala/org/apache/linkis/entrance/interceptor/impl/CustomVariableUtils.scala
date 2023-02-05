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

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.utils.{Logging, Utils, VariableUtils}
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.protocol.variable.{
  RequestQueryAppVariable,
  RequestQueryGlobalVariable,
  ResponseQueryVariable
}
import org.apache.linkis.rpc.Sender

import java.util

object CustomVariableUtils extends Logging {

  /**
   * replace custom variable
   *   1. Get the user-defined variable from the code and replace it 2. If 1 is not done, then get
   *      the user-defined variable from args and replace it. 3. If 2 is not done, get the
   *      user-defined variable from the console and replace it.
   * @param jobRequest
   *   : requestPersistTask
   * @return
   */
  def replaceCustomVar(jobRequest: JobRequest, runType: String): String = {
    val variables: util.Map[String, String] = new util.HashMap[String, String]()
    val sender =
      Sender.getSender(Configuration.CLOUD_CONSOLE_VARIABLE_SPRING_APPLICATION_NAME.getValue)
    val umUser: String = jobRequest.getExecuteUser
    val codeTypeFromLabel = LabelUtil.getCodeType(jobRequest.getLabels)
    val userCreator = LabelUtil.getUserCreator(jobRequest.getLabels)
    val creator: String = if (null != userCreator) userCreator._2 else null
    val requestProtocol = if (null != userCreator) {
      RequestQueryAppVariable(umUser, creator, codeTypeFromLabel)
    } else {
      RequestQueryGlobalVariable(umUser)
    }
    val response: ResponseQueryVariable =
      Utils.tryAndWarn(sender.ask(requestProtocol).asInstanceOf[ResponseQueryVariable])
    if (null != response) {
      val keyAndValue = response.getKeyAndValue
      variables.putAll(keyAndValue)
    }
    val variableMap = TaskUtils
      .getVariableMap(jobRequest.getParams)
      .asInstanceOf[util.HashMap[String, String]]
    variables.putAll(variableMap)
    if (!variables.containsKey("user")) {
      variables.put("user", jobRequest.getExecuteUser)
    }
    VariableUtils.replace(jobRequest.getExecutionCode, runType, variables)
  }

}
