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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import scala.util.control.ControlThrowable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
  private static final Logger logger = LoggerFactory.getLogger(Utils.class);

  public static final ScheduledThreadPoolExecutor defaultScheduler =
      new ScheduledThreadPoolExecutor(20, threadFactory("Linkis-Default-Scheduler-Thread-", true));

  {
    defaultScheduler.setMaximumPoolSize(20);
    defaultScheduler.setKeepAliveTime(5, TimeUnit.MINUTES);
  }

  public static <T> T tryQuietly(Callable<T> tryOp) {
    return tryQuietly(tryOp, t -> {});
  }

  public static <T> T tryCatch(Callable<T> tryOp, Function<Throwable, T> catchOp) {
    T call = null;
    try {
      call = tryOp.call();
    } catch (Throwable t) {
      if (t instanceof ControlThrowable) {
      } else if (t instanceof FatalException) {
        logger.error("Fatal error, system exit...", t);
        return null;
      } else if (t instanceof VirtualMachineError) {
        logger.error("Fatal error, system exit...", t);
        throw (VirtualMachineError) t;
      } else if (t.getCause() instanceof FatalException
          || t.getCause() instanceof VirtualMachineError) {
        logger.error("Caused by fatal error, system exit...", t);
        throw new RuntimeException(t);
      } else if (t instanceof Error) {
        logger.error("Throw error", t);
        throw (Error) t;
      } else {
        call = catchOp.apply(t);
      }
    }
    return call;
  }

  public static void tryCatch(Runnable tryOp, Runnable catchOp) {
    try {
      tryOp.run();
    } catch (Throwable t) {
      if (t instanceof ControlThrowable) {
      } else if (t instanceof FatalException) {
        logger.error("Fatal error, system exit...", t);
      } else if (t instanceof VirtualMachineError) {
        logger.error("Fatal error, system exit...", t);
        throw (VirtualMachineError) t;
      } else if (t.getCause() instanceof FatalException
          || t.getCause() instanceof VirtualMachineError) {
        logger.error("Caused by fatal error, system exit...", t);
        throw new RuntimeException(t);
      } else if (t instanceof Error) {
        logger.error("Throw error", t);
        throw (Error) t;
      } else {
        logger.error("Throw excption", t);
        catchOp.run();
      }
    }
  }

  public static <T> T tryThrow(Callable<T> tryOp, Function<Throwable, Throwable> exception) {
    return tryCatch(
        tryOp,
        t -> {
          try {
            throw exception.apply(t);
          } catch (Throwable throwable) {
            //
            throw new RuntimeException(throwable);
          }
        });
  }

  public static <T> T tryFinally(Callable<T> tryOp, Runnable finallyOp) {
    try {
      return tryOp.call();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      finallyOp.run();
    }
  }

  public static void tryFinally(Runnable tryOp, Runnable finallyOp) {
    try {
      tryOp.run();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      finallyOp.run();
    }
  }

  public static <T> T tryQuietly(Callable<T> tryOp, Consumer<Throwable> catchOp) {
    return tryCatch(
        tryOp,
        t -> {
          catchOp.accept(t);
          return null;
        });
  }

  public static <T> T tryAndWarn(Callable<T> tryOp, Logger log) {
    return tryCatch(
        tryOp,
        t -> {
          if (t instanceof ErrorException) {
            ErrorException error = (ErrorException) t;
            String errorMsg =
                "error code（错误码）: "
                    + error.getErrCode()
                    + ", Error message（错误信息）: "
                    + error.getDesc()
                    + ".";
            log.error(errorMsg, error);
          } else if (t instanceof WarnException) {
            WarnException warn = (WarnException) t;
            String warnMsg =
                "Warning code（警告码）: "
                    + warn.getErrCode()
                    + ", Warning message（警告信息）: "
                    + warn.getDesc()
                    + ".";
            log.warn(warnMsg, warn);
          } else {
            log.warn("", t);
          }
          return null;
        });
  }

  public static void tryAndErrorMsg(Runnable tryOp, String message, Logger log) {
    try {
      tryOp.run();
    } catch (Throwable t) {
      if (t instanceof ControlThrowable) {
      } else if (t instanceof FatalException) {
        logger.error("Fatal error, system exit...", t);
      } else if (t instanceof VirtualMachineError) {
        logger.error("Fatal error, system exit...", t);
        throw (VirtualMachineError) t;
      } else if (t.getCause() instanceof FatalException
          || t.getCause() instanceof VirtualMachineError) {
        logger.error("Caused by fatal error, system exit...", t);
        throw new RuntimeException(t);
      } else if (t instanceof Error) {
        logger.error("Throw error", t);
        throw (Error) t;
      } else {
        log.error(message, t);
      }
    }
  }

  public static <T> void tryAndWarn(Runnable tryOp, Logger log) {
    try {
      tryOp.run();
    } catch (Throwable t) {
      if (t instanceof ControlThrowable) {
      } else if (t instanceof FatalException) {
        log.error("Fatal error, system exit...", t);
      } else if (t instanceof VirtualMachineError) {
        log.error("Fatal error, system exit...", t);
        throw (VirtualMachineError) t;
      } else if (t.getCause() instanceof FatalException
          || t.getCause() instanceof VirtualMachineError) {
        log.error("Caused by fatal error, system exit...", t);
        throw new RuntimeException(t);
      } else if (t instanceof Error) {
        log.error("Throw error", t);
        throw (Error) t;
      }
    }
  }

  public static <T> T tryAndError(Supplier<T> tryOp, Logger log) {
    try {
      return tryOp.get();
    } catch (WarnException warn) {
      String warnMsg =
          "Warning code（警告码）: "
              + warn.getErrCode()
              + ", Warning message（警告信息）: "
              + warn.getDesc()
              + ".";
      log.warn(warnMsg, warn);
      return null;
    } catch (Throwable t) {
      log.error("", t);
      return null;
    }
  }

  public static <T> T tryAndWarnMsg(Callable<T> tryOp, String message, Logger log) {
    return tryCatch(
        tryOp,
        t -> {
          if (t instanceof ErrorException) {
            ErrorException error = (ErrorException) t;
            log.warn(
                "error code（错误码）: "
                    + error.getErrCode()
                    + ", Error message（错误信息）: "
                    + error.getDesc()
                    + ".");
            log.warn(message, error);
          } else if (t instanceof WarnException) {
            WarnException warn = (WarnException) t;
            log.warn(
                "Warning code（警告码）: "
                    + warn.getErrCode()
                    + ", Warning message（警告信息）: "
                    + warn.getDesc()
                    + ".");
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
