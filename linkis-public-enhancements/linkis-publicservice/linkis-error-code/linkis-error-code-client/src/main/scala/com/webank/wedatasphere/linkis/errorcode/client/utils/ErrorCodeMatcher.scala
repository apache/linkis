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

package com.webank.wedatasphere.linkis.errorcode.client.utils

import java.util

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}
import com.webank.wedatasphere.linkis.errorcode.common.LinkisErrorCode


object ErrorCodeMatcher extends Logging{
  def errorMatch(errorCodes:util.List[LinkisErrorCode], log:String):Option[(String, String)] = {
    Utils.tryCatch{
      import scala.collection.JavaConversions._
      errorCodes.foreach(e => if(e.getErrorRegex.findFirstIn(log).isDefined) {
        val matched = e.getErrorRegex.unapplySeq(log)
        if(matched.nonEmpty)
          return Some(e.getErrorCode -> e.getErrorDesc.format(matched.get:_*))
        else return Some(e.getErrorCode -> e.getErrorDesc)
      })
      None
    }{
      t:Throwable => error("failed to match error code", t)
        None
    }

  }
}
