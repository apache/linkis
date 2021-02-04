/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.entrance.interceptor.impl

import java.lang

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor
import com.webank.wedatasphere.linkis.entrance.interceptor.exception.ScalaCodeCheckException
import com.webank.wedatasphere.linkis.protocol.query.RequestPersistTask
import com.webank.wedatasphere.linkis.protocol.task.Task

/**
  * created by enjoyyin on 2019/2/25
  * Description:
  */
class ScalaCodeInterceptor extends EntranceInterceptor {

  private val SCALA_TYPE = "scala"

  override def apply(task: Task, logAppender: lang.StringBuilder): Task = task match{
    case requestPersistTask:RequestPersistTask => val error = new StringBuilder
      requestPersistTask.getRunType match {
        case SCALA_TYPE => Utils.tryThrow(ScalaExplain.authPass(requestPersistTask.getExecutionCode, error)){
          case ScalaCodeCheckException(errorCode, errDesc) => requestPersistTask.setErrCode(errorCode)
            requestPersistTask.setErrDesc(errDesc)
            ScalaCodeCheckException(errorCode, errDesc)
          case t:Throwable => val exception = ScalaCodeCheckException(20074, "Scala code check failed(scala代码检查失败)")
            exception.initCause(t)
            exception
        }
        case _ =>
      }
      requestPersistTask
  }
}
