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

package org.apache.linkis.errorcode.client.utils

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.errorcode.common.LinkisErrorCode

import java.util

object ErrorCodeMatcher extends Logging {

  def errorMatch(errorCodes: util.List[LinkisErrorCode], log: String): Option[(String, String)] = {
    Utils.tryCatch {
      import scala.collection.JavaConverters._
      errorCodes.asScala.foreach(e =>
        if (e.getErrorRegex.findFirstIn(log).isDefined) {
          val matched = e.getErrorRegex.unapplySeq(log)
          if (matched.nonEmpty) {
            return Some(e.getErrorCode -> e.getErrorDesc.format(matched.get: _*))
          } else return Some(e.getErrorCode -> e.getErrorDesc)
        }
      )
      None
    } { t: Throwable =>
      logger.error("failed to match error code", t)
      None
    }

  }

}
