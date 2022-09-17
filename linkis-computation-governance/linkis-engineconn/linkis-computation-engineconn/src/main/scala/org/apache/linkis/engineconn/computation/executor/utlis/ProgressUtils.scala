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

package org.apache.linkis.engineconn.computation.executor.utlis

import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext

object ProgressUtils {

  private val OLD_PROGRESS_KEY = "oldProgress"

  def getOldProgress(engineExecutionContext: EngineExecutionContext): Float = {
    if (null == engineExecutionContext) {
      0f
    } else {
      val value = engineExecutionContext.getProperties.get(OLD_PROGRESS_KEY)
      if (null == value) 0f else value.asInstanceOf[Float]
    }
  }

  def putProgress(newProgress: Float, engineExecutionContext: EngineExecutionContext): Unit = {
    if (null != engineExecutionContext) {
      engineExecutionContext.getProperties.put(OLD_PROGRESS_KEY, newProgress.asInstanceOf[AnyRef])
    }
  }

}
