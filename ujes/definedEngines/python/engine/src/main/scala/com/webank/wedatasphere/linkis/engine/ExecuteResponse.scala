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

package com.webank.wedatasphere.linkis.engine

import com.webank.wedatasphere.linkis.engine.exception.QueryFailedException
import org.json4s.JValue
import org.json4s.JsonAST.JString
/**
  * Created by allenlliu on 2019/4/8.
  */

sealed abstract class ExecuteResponse()
case class ExecuteComplete(value: JValue) extends ExecuteResponse() {
  def this(output: String) = this(JString(output))
}
object ExecuteComplete {
  def apply(output: String) = new ExecuteComplete(output)
  //  def apply(value: JValue) = new ExecuteComplete(value)
}
case class ExecuteIncomplete(output: String) extends ExecuteResponse()
case class ExecuteError(t: Throwable) extends ExecuteResponse() {
  def this(errorMsg: String) = this(new QueryFailedException(60001,errorMsg))
}
