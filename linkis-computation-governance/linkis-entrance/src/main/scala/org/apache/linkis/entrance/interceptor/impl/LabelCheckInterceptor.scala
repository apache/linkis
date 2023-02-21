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

import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.entrance.interceptor.exception.LabelCheckException
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.manager.label.entity.engine.{EngineTypeLabel, UserCreatorLabel}

import org.apache.commons.lang3.StringUtils

import java.lang

import scala.collection.JavaConverters._

class LabelCheckInterceptor extends EntranceInterceptor {

  /**
   * The apply function is to supplement the information of the incoming parameter task, making the
   * content of this task more complete.    * Additional information includes: database information
   * supplement, custom variable substitution, code check, limit limit, etc.
   * apply函数是对传入参数task进行信息的补充，使得这个task的内容更加完整。 补充的信息包括: 数据库信息补充、自定义变量替换、代码检查、limit限制等
   *
   * @param jobRequest
   * @param logAppender
   *   Used to cache the necessary reminder logs and pass them to the upper layer(用于缓存必要的提醒日志，传给上层)
   * @return
   */
  override def apply(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    jobRequest match {
      case requestPersistTask: JobRequest =>
        val labels = requestPersistTask.getLabels
        checkEngineTypeLabel(labels)
        checkUserCreatorLabel(labels, jobRequest.getSubmitUser, jobRequest.getExecuteUser)
        jobRequest
      case _ => jobRequest
    }
  }

  private def checkEngineTypeLabel(labels: java.util.List[Label[_]]): Unit = {
    val engineTypeLabelOption = labels.asScala.find(_.isInstanceOf[EngineTypeLabel])
    if (engineTypeLabelOption.isDefined) {
      val engineLabel = engineTypeLabelOption.get.asInstanceOf[EngineTypeLabel]
      if (StringUtils.isNotBlank(engineLabel.getEngineType)) {
        return
      }
    }
    throw LabelCheckException(50079, "engineTypeLabel must be need")
  }

  private def checkUserCreatorLabel(
      labels: java.util.List[Label[_]],
      submitUser: String,
      executeUser: String
  ): Unit = {
    val userCreatorLabelOption = labels.asScala.find(_.isInstanceOf[UserCreatorLabel])
    if (userCreatorLabelOption.isDefined) {
      val userCreator = userCreatorLabelOption.get.asInstanceOf[UserCreatorLabel]
      if (StringUtils.isNotBlank(userCreator.getUser)) {
        val userInLabel = userCreator.getUser
        if (userInLabel.equalsIgnoreCase(executeUser)) {
          return
        } else {
          throw LabelCheckException(
            50080,
            s"SubmitUser : ${submitUser} must be the same as ExecuteUser : ${executeUser} , and user : ${userInLabel} in userCreatorLabel."
          )
        }
      }
      // TODO: need one more check to see if userCreatorLabel equals executeUser
    }
    throw LabelCheckException(50079, "UserCreatorLabel must be need")
  }

}
