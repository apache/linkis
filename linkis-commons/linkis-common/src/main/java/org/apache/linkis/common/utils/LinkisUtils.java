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

package org.apache.linkis.common.utils;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.common.exception.FatalException;
import org.apache.linkis.common.exception.WarnException;

import java.util.concurrent.Callable;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisUtils {
  private static final Logger logger = LoggerFactory.getLogger(LinkisUtils.class);

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

  public static String getJvmUser() {
    return System.getProperty("user.name");
  }
}
