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

package org.apache.linkis.scheduler.future

import org.apache.linkis.common.utils.{Logging, Utils}

import org.apache.commons.lang3.reflect.FieldUtils

import java.util.concurrent.{Future, FutureTask}

class BDPFutureTask(future: Future[_]) extends BDPFuture with Logging {

  override def cancel(): Unit = Utils.tryAndErrorMsg {
    future match {
      case futureTask: FutureTask[_] =>
        logger.info("Start to interrupt BDPFutureTask")
        val runner = FieldUtils
          .readDeclaredField(futureTask, "runner", true)
          .asInstanceOf[Thread]
        runner.interrupt()
        logger.info(s"Finished to interrupt BDPFutureTask of ${runner.getName}")
    }
  }("Failed to interrupt BDPFutureTask")

}
