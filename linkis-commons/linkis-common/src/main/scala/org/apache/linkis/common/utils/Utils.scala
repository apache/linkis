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

package org.apache.linkis.common.utils

import org.apache.linkis.common.exception.{
  ErrorException,
  FatalException,
  LinkisCommonErrorException,
  WarnException
}

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.SystemUtils

import java.io.{BufferedReader, InputStreamReader}
import java.lang.management.ManagementFactory
import java.net.InetAddress
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicInteger

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}
import scala.concurrent.duration.Duration
import scala.util.control.ControlThrowable

import org.slf4j.Logger

object Utils extends Logging {

  val DEFAULE_SCHEDULER_THREAD_NAME_PREFIX = "Linkis-Default-Scheduler-Thread-"

  def tryQuietly[T](tryOp: => T): T = tryQuietly(tryOp, _ => ())

  def tryCatch[T](tryOp: => T)(catchOp: Throwable => T): T = {
    try tryOp
    catch {
      case t: ControlThrowable => throw t
      case fatal: FatalException =>
        logger.error("Fatal error, system exit...", fatal)
        System.exit(fatal.getErrCode)
        null.asInstanceOf[T]
      case e: VirtualMachineError =>
        logger.error("Fatal error, system exit...", e)
        System.exit(-1)
        throw e
      case exp
          if (null != exp.getCause && (exp.getCause.isInstanceOf[FatalException] || exp.getCause
            .isInstanceOf[VirtualMachineError])) =>
        logger.error("Caused by fatal error, system exit...", exp)
        System.exit(-1)
        throw exp
      case er: Error =>
        logger.error("Throw error", er)
        throw er
      case t => catchOp(t)
    }
  }

  def tryThrow[T](tryOp: => T)(exception: Throwable => Throwable): T = tryCatch(tryOp) {
    t: Throwable => throw exception(t)
  }

  def tryFinally[T](tryOp: => T)(finallyOp: => Unit): T = try tryOp
  finally finallyOp

  def tryQuietly[T](tryOp: => T, catchOp: Throwable => Unit): T = tryCatch(tryOp) { t =>
    catchOp(t)
    null.asInstanceOf[T]
  }

  def tryAndWarn[T](tryOp: => T)(implicit log: Logger): T = tryCatch(tryOp) {
    case error: ErrorException =>
      val errorMsg =
        s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}."
      log.error(errorMsg, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      val warnMsg =
        s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}."
      log.warn(warnMsg, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.warn("", t)
      null.asInstanceOf[T]
  }

  def tryAndWarnMsg[T](tryOp: => T)(message: String)(implicit log: Logger): T = tryCatch(tryOp) {
    case error: ErrorException =>
      log.warn(s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}.")
      log.warn(message, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      log.warn(s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}.")
      log.warn(message, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.warn(message, t)
      null.asInstanceOf[T]
  }

  def tryAndError[T](tryOp: => T)(implicit log: Logger): T = tryCatch(tryOp) {
    case error: ErrorException =>
      val errorMsg =
        s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}."
      log.error(errorMsg, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      val warnMsg =
        s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}."
      log.warn(warnMsg, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.error("", t)
      null.asInstanceOf[T]
  }

  def tryAndErrorMsg[T](tryOp: => T)(message: String)(implicit log: Logger): T = tryCatch(tryOp) {
    case error: ErrorException =>
      log.error(s"error code（错误码）: ${error.getErrCode}, Error message（错误信息）: ${error.getDesc}.")
      log.error(message, error)
      null.asInstanceOf[T]
    case warn: WarnException =>
      log.warn(s"Warning code（警告码）: ${warn.getErrCode}, Warning message（警告信息）: ${warn.getDesc}.")
      log.warn(message, warn)
      null.asInstanceOf[T]
    case t: Throwable =>
      log.error(message, t)
      null.asInstanceOf[T]
  }

  def sleepQuietly(mills: Long): Unit = tryQuietly(Thread.sleep(mills))

  def threadFactory(threadName: String, isDaemon: Boolean = true): ThreadFactory = {
    new ThreadFactory {
      val num = new AtomicInteger(0)

      override def newThread(r: Runnable): Thread = {
        val t = new Thread(r)
        t.setDaemon(isDaemon)
        t.setName(threadName + num.incrementAndGet())
        t
      }
    }
  }

  def newCachedThreadPool(
      threadNum: Int,
      threadName: String,
      isDaemon: Boolean = true
  ): ThreadPoolExecutor = {
    val threadPool = new ThreadPoolExecutor(
      threadNum,
      threadNum,
      120L,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable](10 * threadNum),
      threadFactory(threadName, isDaemon)
    )
    threadPool.allowCoreThreadTimeOut(true)
    threadPool
  }

  def newCachedExecutionContext(
      threadNum: Int,
      threadName: String,
      isDaemon: Boolean = true
  ): ExecutionContextExecutorService =
    ExecutionContext.fromExecutorService(newCachedThreadPool(threadNum, threadName, isDaemon))

  def newFixedThreadPool(
      threadNum: Int,
      threadName: String,
      isDaemon: Boolean = true
  ): ExecutorService = {
    Executors.newFixedThreadPool(threadNum, threadFactory(threadName, isDaemon))
  }

  def newFixedExecutionContext(
      threadNum: Int,
      threadName: String,
      isDaemon: Boolean = true
  ): ExecutionContextExecutorService = {
    ExecutionContext.fromExecutorService(newFixedThreadPool(threadNum, threadName, isDaemon))
  }

  val defaultScheduler: ScheduledThreadPoolExecutor = {
    val scheduler =
      new ScheduledThreadPoolExecutor(20, threadFactory(DEFAULE_SCHEDULER_THREAD_NAME_PREFIX, true))
    scheduler.setMaximumPoolSize(20)
    scheduler.setKeepAliveTime(5, TimeUnit.MINUTES)
    scheduler
  }

  def getLocalHostname: String = InetAddress.getLocalHost.getHostAddress

  def getComputerName: String =
    Utils.tryCatch(InetAddress.getLocalHost.getCanonicalHostName)(_ => SystemUtils.getHostName)

  /**
   * Checks if event has occurred during some time period. This performs an exponential backoff to
   * limit the poll calls.
   *
   * @param checkForEvent
   *   event to check, until it is true
   * @param atMost
   *   most wait time
   * @throws java.util.concurrent.TimeoutException
   *   throws this exception when it is timeout
   * @throws java.lang.InterruptedException
   *   throws this exception when it is interrupted
   * @return
   */
  @throws(classOf[TimeoutException])
  @throws(classOf[InterruptedException])
  final def waitUntil(
      checkForEvent: () => Boolean,
      atMost: Duration,
      radix: Int,
      maxPeriod: Long
  ): Unit = {
    val endTime =
      try System.currentTimeMillis() + atMost.toMillis
      catch {
        case _: IllegalArgumentException => 0L
      }

    @tailrec
    def aux(count: Int): Unit = {
      if (!checkForEvent()) {
        val now = System.currentTimeMillis()

        if (endTime == 0 || now < endTime) {
          val sleepTime = Math.max(Math.min(radix * count, maxPeriod), 100)
          Thread.sleep(sleepTime)
          aux(count + 1)
        } else {
          throw new TimeoutException
        }
      }
    }

    aux(1)
  }

  final def waitUntil(checkForEvent: () => Boolean, atMost: Duration): Unit =
    waitUntil(checkForEvent, atMost, 100, 2000)

  /**
   * do not exec complex shell command with lots of output, may cause io blocking
   *
   * @param commandLine
   *   shell command
   * @return
   */
  def exec(commandLine: Array[String]): String = exec(commandLine, -1)

  /**
   * do not exec complex shell command with lots of output, may cause io blocking
   *
   * @param commandLine
   *   shell command
   * @return
   */
  def exec(commandLine: List[String]): String = exec(commandLine, -1)

  /**
   * do not exec complex shell command with lots of output, may cause io blocking
   *
   * @param commandLine
   *   shell command
   * @param maxWaitTime
   *   max wait time
   * @return
   */
  def exec(commandLine: Array[String], maxWaitTime: Long): String =
    exec(commandLine.toList, maxWaitTime)

  /**
   * do not exec complex shell command with lots of output, may cause io blocking
   *
   * @param commandLine
   *   shell command
   * @param maxWaitTime
   *   max wait time
   * @return
   */
  def exec(commandLine: List[String], maxWaitTime: Long): String = {
    val pb = new ProcessBuilder(commandLine.asJava)
    pb.redirectErrorStream(true)
    pb.redirectInput(ProcessBuilder.Redirect.PIPE)
    val process = pb.start
    val log = new BufferedReader(new InputStreamReader(process.getInputStream))
    val exitCode = if (maxWaitTime > 0) {
      val completed = process.waitFor(maxWaitTime, TimeUnit.MILLISECONDS)
      if (!completed) {
        IOUtils.closeQuietly(log)
        process.destroy()
        throw new TimeoutException(
          s"exec timeout with ${ByteTimeUtils.msDurationToString(maxWaitTime)}!"
        )
      }
      process.exitValue
    } else {
      tryThrow(process.waitFor)(t => {
        process.destroy(); IOUtils.closeQuietly(log); t
      })
    }
    val lines = log.lines().toArray
    IOUtils.closeQuietly(log)
    if (exitCode != 0) {
      throw new LinkisCommonErrorException(
        0,
        s"exec failed with exit code: $exitCode, ${lines.mkString(". ")}"
      )
    }
    lines.mkString("\n")
  }

  def addShutdownHook(hook: => Unit): Unit = ShutdownHookManager.addShutdownHook(hook)

  def getClassInstance[T](className: String): T = {
    Utils.tryThrow(
      Thread.currentThread.getContextClassLoader
        .loadClass(className)
        .asInstanceOf[Class[T]]
        .newInstance()
    )(t => {
      logger.error(s"Failed to instance: $className ", t)
      throw t
    })
  }

  @deprecated("use ByteTimeUtils.msDurationToString method")
  def msDurationToString(ms: Long): String = {
    val second = 1000
    val minute = 60 * second
    val hour = 60 * minute
    ms match {
      case t if t < second =>
        "%d ms".format(t)
      case t if t < minute =>
        "%.1f 秒".format(t.toFloat / second)
      case t if t < hour =>
        "%.1f 分钟".format(t.toFloat / minute)
      case t =>
        "%.2f 小时".format(t.toFloat / hour)
    }
  }

  def getJvmUser: String = System.getProperty("user.name")

  // Note: may fail in some JVM implementations
  def getProcessId(): String = {
    // therefore fallback has to be provided
    // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
    val jvmName = ManagementFactory.getRuntimeMXBean.getName
    val index = jvmName.indexOf('@')
    // part before '@' empty (index = 0) / '@' not found (index = -1)
    if (index < 1) {
      null
    }
    Utils.tryCatch {
      val getpid = jvmName.substring(0, index)
      logger.info(s"get java process Id:$getpid")
      getpid
    } { t =>
      logger.info(s"Failed to get process Id with error", t.getMessage)
      null
    }
  }

}
