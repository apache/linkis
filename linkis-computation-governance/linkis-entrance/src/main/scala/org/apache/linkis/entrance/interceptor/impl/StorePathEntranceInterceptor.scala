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

import org.apache.linkis.common.utils.Logging
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.exception.{EntranceErrorCode, EntranceErrorException}
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.time.DateFormatUtils

import java.util

import scala.collection.JavaConverters.{asScalaBufferConverter, mapAsScalaMapConverter}

class StorePathEntranceInterceptor extends EntranceInterceptor with Logging {

  /**
   * The apply function is to supplement the information of the incoming parameter task, making the
   * content of this task more complete. Additional information includes: database information
   * supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
   *
   * @param jobReq
   * @return
   */
  override def apply(jobReq: JobRequest, logAppender: java.lang.StringBuilder): JobRequest = {
    val paramsMap = if (null != jobReq.getParams) {
      jobReq.getParams
    } else {
      new util.HashMap[String, AnyRef]()
    }
    var runtimeMap = TaskUtils.getRuntimeMap(paramsMap)
    if (null == runtimeMap || runtimeMap.isEmpty) {
      runtimeMap = new util.HashMap[String, AnyRef]()
    }
    if (runtimeMap.containsKey(GovernanceCommonConf.RESULT_SET_STORE_PATH.key)) {
      return jobReq
    }
    if (EntranceConfiguration.ENABLE_HDFS_RES_DIR_PRIVATE) {
      val parentPath = generateUserPrivateResDir(jobReq)
      runtimeMap.put(GovernanceCommonConf.RESULT_SET_STORE_PATH.key, parentPath)
      TaskUtils.addRuntimeMap(paramsMap, runtimeMap)
      val params = new util.HashMap[String, AnyRef]()
      paramsMap.asScala.foreach(kv => params.put(kv._1, kv._2))
      jobReq.setResultLocation(parentPath)
      jobReq.setParams(params)
      jobReq
    } else {
      jobReq
    }

  }

  private def generateUserPrivateResDir(jobReq: JobRequest): String = {
    var parentPath: String = GovernanceCommonConf.RESULT_SET_STORE_PATH.getValue
    if (!parentPath.endsWith("/")) parentPath += "/"
    parentPath += jobReq.getExecuteUser
    if (!parentPath.endsWith("/")) parentPath += "/linkis/"
    else parentPath += "linkis/"
    val userCreator = LabelUtil.getUserCreator(jobReq.getLabels)
    if (null == userCreator) {
      val labelJson =
        BDPJettyServerHelper.gson.toJson(jobReq.getLabels.asScala.filter(_ != null).map(_.toString))
      throw new EntranceErrorException(
        EntranceErrorCode.LABEL_PARAMS_INVALID.getErrCode,
        s"UserCreator cannot be empty in labels : ${labelJson} of job with id : ${jobReq.getId}"
      )
    }
    // multi linkis cluster should not use same root folder , in which case result file may be overwrite
    parentPath += DateFormatUtils.format(System.currentTimeMillis, "yyyy-MM-dd/HHmmss") + "/" +
      userCreator._2 + "/" + jobReq.getId
    parentPath
  }

}
