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

package com.webank.wedatasphere.linkis.scheduler.future

import java.util.concurrent.{Future, FutureTask}

import com.webank.wedatasphere.linkis.common.utils.{Logging, Utils}


class BDPFutureTask(future: Future[_]) extends BDPFuture with Logging {
  override def cancel(): Unit = Utils.tryAndErrorMsg {
    future match {
      case futureTask: FutureTask[_] =>
        info("Start to interrupt BDPFutureTask")
        val futureType = futureTask.getClass
        val field = futureType.getDeclaredField("runner")
        field.setAccessible(true)
        val runner = field.get(futureTask).asInstanceOf[Thread]
        runner.interrupt()
        info(s"Finished to interrupt BDPFutureTask of ${runner.getName}")
    }
  }("Failed to interrupt BDPFutureTask")
}
