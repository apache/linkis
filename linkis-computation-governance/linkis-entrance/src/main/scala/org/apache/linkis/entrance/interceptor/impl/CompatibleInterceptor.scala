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
import org.apache.linkis.entrance.interceptor.EntranceInterceptor
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.protocol.utils.TaskUtils

import java.lang

/**
 * Compatible with linkis 0.X client
 */
class CompatibleInterceptor extends EntranceInterceptor with Logging {

  private val oldQueueKey = "wds.linkis.yarnqueue"
  private val newQueueKey = "wds.linkis.rm.yarnqueue"

  override def apply(task: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    val startMap = TaskUtils.getStartupMap(task.getParams)
    if (null != startMap && startMap.containsKey(oldQueueKey)) {
      logger.info(
        s"Compatible with queue parameters, the queue $newQueueKey will be set to ${startMap.get(oldQueueKey)}"
      )
      startMap.put(newQueueKey, startMap.get(oldQueueKey))
    }
    task
  }

}
