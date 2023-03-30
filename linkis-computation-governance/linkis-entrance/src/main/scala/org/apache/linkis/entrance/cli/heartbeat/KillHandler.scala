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

package org.apache.linkis.entrance.cli.heartbeat

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.entrance.execute.EntranceJob

class KillHandler extends HeartbeatLossHandler with Logging {

  override def handle(jobs: List[EntranceJob]): Unit = {
    for (job <- jobs) {
      if (job != null) {
        logger.info("Killing job: " + job.getId)
        Utils.tryCatch(
          job.onFailure("Job is killed because of client-server connection lost", null)
        ) { t =>
          logger.error("failed to kill job: " + job.getId, t)
        }
      }
    }
  }

}
