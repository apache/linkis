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

package org.apache.linkis.jdbc.hook

import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.{Logging, Utils}

import scala.collection.mutable.ArrayBuffer

trait JDBCDriverPreExecutionHook {

  def callPreExecutionHook(sql: String): String

}

object JDBCDriverPreExecutionHook extends Logging {

  private val HOOKSCONF = CommonVars(
    "wds.linkis.jdbc.pre.hook.class",
    "org.apache.linkis.jdbc.hook.impl.TableauPreExecutionHook," +
      "org.apache.linkis.jdbc.hook.impl.NoLimitExecutionHook"
  )

  private val preExecutionHooks: Array[JDBCDriverPreExecutionHook] = {
    val hooks = new ArrayBuffer[JDBCDriverPreExecutionHook]()
    HOOKSCONF.getValue.split(",") foreach { hookStr =>
      Utils.tryAndWarnMsg {
        hooks += Utils.getClassInstance[JDBCDriverPreExecutionHook](hookStr)
      }(s"failed to load class ${hookStr}")
    }
    hooks.toArray
  }

  def getPreExecutionHooks: Array[JDBCDriverPreExecutionHook] = preExecutionHooks
}
