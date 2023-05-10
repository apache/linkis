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

package org.apache.linkis.manager.am.util;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.FatalException;
import org.apache.linkis.common.exception.WarnException;

import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisUtils {
  private static final Logger logger = LoggerFactory.getLogger(LinkisUtils.class);

  public static final ScheduledThreadPoolExecutor defaultScheduler =
      new ScheduledThreadPoolExecutor(20, threadFactory("Linkis-Default-Scheduler-Thread-", true));

  {
    defaultScheduler.setMaximumPoolSize(20);
    defaultScheduler.setKeepAliveTime(5, TimeUnit.MINUTES);
  }

  public static <T> T tryCatch(Callable<T> tryOp, Function<Throwable, T> catchOp) {
    T result = null;
    try {
      result = tryOp.call();
    } catch (Throwable t) {
      if (t instanceof FatalException) {
        logger.error("Fatal error, system exit...", t);
        System.exit(((FatalException) t).getErrCode());
      } else if (t instanceof VirtualMachineError) {
        logger.error("Fatal error, system exit...", t);
        System.exit(-1);
      } else if (null != t.getCause()
          && (t.getCause() instanceof FatalException
              || t.getCause() instanceof VirtualMachineError)) {
        logger.error("Caused by fatal error, system exit...", t);
        System.exit(-1);
      } else if (t instanceof Error) {
        logger.error("Throw error", t);
        throw (Error) t;
      } else {
        result = catchOp.apply(t);
      }
    }
    return result;
  }

  public static void tryFinally(Runnable tryOp, Runnable finallyOp) {
    try {
      tryOp.run();
    } finally {
      finallyOp.run();
    }
  }

  public static <T> T tryAndWarn(Callable<T> tryOp, Logger log) {
    return tryCatch(
        tryOp,
        t -> {
          if (t instanceof ErrorException) {
            ErrorException error = (ErrorException) t;
            log.error(
                "Warning code（警告码）: {}, Warning message（警告信息）: {}.",
                error.getErrCode(),
                error.getDesc(),
                error);

          } else if (t instanceof WarnException) {
            WarnException warn = (WarnException) t;
            log.warn(
                "Warning code（警告码）: {}, Warning message（警告信息）: {}.",
                warn.getErrCode(),
                warn.getDesc(),
                warn);

          } else {
            log.warn("", t);
          }
          return null;
        });
  }

  public static void tryAndErrorMsg(Runnable tryOp, String message, Logger log) {
    try {
      tryOp.run();
    } catch (WarnException t) {
      WarnException warn = (WarnException) t;
      log.warn(
          "Warning code（警告码）: {}, Warning message（警告信息）: {}.", warn.getErrCode(), warn.getDesc());
      log.warn(message, warn);
    } catch (Exception t) {
      log.warn(message, t);
    }
  }

  public static <T> void tryAndWarn(Runnable tryOp, Logger log) {
    try {
      tryOp.run();
    } catch (Throwable error) {
      if (error instanceof WarnException) {
        WarnException warn = (WarnException) error;
        log.warn(
            "Warning code（警告码）: {}, Warning message（警告信息）: {}.",
            warn.getErrCode(),
            warn.getDesc(),
            error);
      } else {
        log.warn("", error);
      }
    }
  }

  public static void tryAndWarnMsg(Runnable tryOp, String message, Logger log) {
    try {
      tryOp.run();
    } catch (WarnException t) {
      WarnException warn = (WarnException) t;
      log.warn(
          "Warning code（警告码）: {}, Warning message（警告信息）: {}.", warn.getErrCode(), warn.getDesc());
      log.warn(message, warn);
    } catch (Exception t) {
      log.warn(message, t);
    }
  }

  public static <T> T tryAndWarnMsg(Callable<T> tryOp, String message, Logger log) {
    return tryCatch(
        tryOp,
        t -> {
          if (t instanceof ErrorException) {
            ErrorException error = (ErrorException) t;
            log.warn(
                "Warning code（警告码）: {}, Warning message（警告信息）: {}.",
                error.getErrCode(),
                error.getDesc());
            log.warn(message, error);
          } else if (t instanceof WarnException) {
            WarnException warn = (WarnException) t;
            log.warn(
                "Warning code（警告码）: {}, Warning message（警告信息）: {}.",
                warn.getErrCode(),
                warn.getDesc());
            log.warn(message, warn);
          } else {
            log.warn(message, t);
          }
          return null;
        });
  }

  /**
   * Checks if event has occurred during some time period. This performs an exponential backoff to
   * limit the poll calls.
   *
   * @param checkForEvent event to check, until it is true
   * @param atMost most wait time
   * @return
   * @throws java.util.concurrent.TimeoutException throws this exception when it is timeout
   * @throws java.lang.InterruptedException throws this exception when it is interrupted
   */
  public static void waitUntil(
      Supplier<Boolean> checkForEvent, Duration atMost, int radix, long maxPeriod)
      throws TimeoutException, InterruptedException {
    long endTime;
    try {
      endTime = System.currentTimeMillis() + atMost.toMillis();
    } catch (IllegalArgumentException e) {
      endTime = 0L;
    }

    int count = 1;
    while (!checkForEvent.get()) {
      long now = System.currentTimeMillis();
      if (endTime == 0 || now < endTime) {
        long sleepTime = Math.max(Math.min(radix * count, maxPeriod), 100);
        TimeUnit.MILLISECONDS.sleep(sleepTime);
        count++;
      } else {
        throw new TimeoutException();
      }
    }
  }

  public static void waitUntil(Supplier<Boolean> checkForEvent, Duration atMost)
      throws TimeoutException, InterruptedException {
    waitUntil(checkForEvent, atMost, 100, 2000);
  }

  public static ExecutorService newFixedThreadPool(
      int threadNum, String threadName, boolean isDaemon) {
    ThreadFactory threadFactory = threadFactory(threadName, isDaemon);
    return Executors.newFixedThreadPool(threadNum, threadFactory);
  }

  public static ThreadPoolExecutor newCachedThreadPool(
      Integer threadNum, String threadName, Boolean isDaemon) {
    ThreadPoolExecutor threadPool =
        new ThreadPoolExecutor(
            threadNum,
            threadNum,
            120L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10 * threadNum),
            threadFactory(threadName, isDaemon));
    threadPool.allowCoreThreadTimeOut(true);
    return threadPool;
  }

  private static ThreadFactory threadFactory(String threadName, boolean isDaemon) {
    return new ThreadFactory() {
      AtomicInteger num = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(isDaemon);
        t.setName(threadName + num.getAndIncrement());
        return t;
      }
    };
  }

  public static String getJvmUser() {
    return System.getProperty("user.name");
  }
}
