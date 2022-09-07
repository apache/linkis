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

import org.apache.linkis.common.utils.Utils

import java.util.concurrent.{ExecutorService, Future}

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BDPFutureTaskTest {
  private var future: Future[_] = _
  private var bdpFutureTask: BDPFuture = _

  @Test
  def testBDPFutureTask: Unit = {
    val executeService: ExecutorService =
      Utils.newCachedThreadPool(1, "test" + "-ThreadPool-", true)

    val runnable = new Thread(new Runnable {
      override def run(): Unit = Thread.sleep(1000)
    })

    future = executeService.submit(runnable)
    bdpFutureTask = new BDPFutureTask(future)
    if (runnable.isAlive) {
      assertEquals(false, runnable.isInterrupted)
      bdpFutureTask.cancel()
      assertEquals(true, runnable.isInterrupted)
    }

  }

}
