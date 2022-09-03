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

package org.apache.linkis.computation.client.job

import org.apache.linkis.common.utils.{Logging, Utils}
import org.apache.linkis.computation.client.{JobListener, LinkisJob, LinkisJobBuilder}
import org.apache.linkis.computation.client.operator.{Operator, OperatorFactory}
import org.apache.linkis.ujes.client.exception.UJESJobException

import java.util.concurrent.{Future, TimeoutException, TimeUnit}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration

trait AbstractLinkisJob extends LinkisJob with Logging {

  private var future: Future[_] = _
  private var killed = false
  private val jobListeners: ArrayBuffer[JobListener] = new ArrayBuffer[JobListener]()

  private val operatorActions: ArrayBuffer[Operator[_] => Operator[_]] =
    new ArrayBuffer[Operator[_] => Operator[_]]()

  protected def wrapperObj[T](obj: Object, errorMsg: String)(op: => T): T = {
    if (obj == null) throw new UJESJobException(errorMsg)
    op
  }

  protected def getJobListeners: Array[JobListener] = jobListeners.toArray

  protected val getPeriod: Long = 1000

  protected val maxFailedNum = 3

  def addJobListener(jobListener: JobListener): Unit = {
    jobListeners += jobListener
    initJobDaemon()
  }

  protected def initJobDaemon(): Unit = if (future == null) jobListeners synchronized {
    if (future == null) {
      future = LinkisJobBuilder.getThreadPoolExecutor.scheduleAtFixedRate(
        new Runnable {
          private var failedNum = 0
          override def run(): Unit = {
            var exception: Throwable = null
            val isJobCompleted = Utils.tryCatch(isCompleted) { t =>
              exception = t
              failedNum += 1
              if (failedNum >= maxFailedNum) {
                logger.error(
                  s"Get Job-$getId status failed for $failedNum tries, now mark this job failed.",
                  t
                )
                Utils.tryAndWarn(
                  jobListeners.foreach(_.onJobUnknownError(AbstractLinkisJob.this, t))
                )
                true
              } else {
                logger.warn(s"Get Job-$getId status failed, wait for the $failedNum+ retries.", t)
                false
              }
            }
            if (exception == null) failedNum = 0
            if (isJobCompleted) future.cancel(true)
          }
        },
        getPeriod,
        getPeriod,
        TimeUnit.MILLISECONDS
      )
    }
  }

  override final def getOperator(operatorName: String): Operator[_] = {
    var operator = OperatorFactory().createOperatorByName(operatorName)
    operatorActions.foreach(action => operator = action(operator))
    operator.initOperator(this)
    operator
//    operatorActions.foldLeft(operator)((operator, action) => action(operator))
  }

  protected def addOperatorAction(operatorAction: Operator[_] => Operator[_]): Unit =
    operatorActions += operatorAction

  override def kill(): Unit = {
    logger.info(s"User try to kill Job-$getId.")
    doKill()
    if (future != null) future.cancel(true)
    killed = true
  }

  protected def doKill(): Unit

  @throws(classOf[UJESJobException])
  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  override def waitFor(mills: Long): Unit = {
    val duration = if (mills > 0) Duration(mills, TimeUnit.MILLISECONDS) else Duration.Inf
    Utils.waitUntil(() => killed || isCompleted, duration, 500, 5000)
    if (killed) throw new UJESJobException(s"$getId is killed!")
  }

  override def waitForCompleted(): Unit = waitFor(-1)

}
