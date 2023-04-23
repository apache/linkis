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

package org.apache.linkis.cli.application.utils;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerManager {
  private static ExecutorService fixedThreadPool;
  private static ThreadPoolExecutor cachedThreadPool;
  private static int THREAD_NUM = 5;
  private static String THREAD_NAME = "LinkisCli-Scheduler";
  private static Boolean IS_DEAMON = false;

  public static ThreadFactory threadFactory(String threadName, Boolean isDaemon) {
    return new ThreadFactory() {
      AtomicInteger num = new AtomicInteger(0);

      @Override
      public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(isDaemon);
        t.setName(threadName + num.incrementAndGet());
        return t;
      }
    };
  }

  public static ThreadPoolExecutor newCachedThreadPool(
      int threadNum, String threadName, Boolean isDaemon) {
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

  public static ExecutorService newFixedThreadPool(
      int threadNum, String threadName, Boolean isDaemon) {
    return Executors.newFixedThreadPool(threadNum, threadFactory(threadName, isDaemon));
  }

  public static ThreadPoolExecutor getCachedThreadPoolExecutor() {
    if (cachedThreadPool == null) {
      synchronized (SchedulerManager.class) {
        if (cachedThreadPool == null) {
          cachedThreadPool = newCachedThreadPool(THREAD_NUM, THREAD_NAME, IS_DEAMON);
        }
      }
    }
    return cachedThreadPool;
  }

  public static ExecutorService getFixedThreadPool() {
    if (fixedThreadPool == null) {
      synchronized (SchedulerManager.class) {
        if (fixedThreadPool == null) {
          fixedThreadPool = newFixedThreadPool(THREAD_NUM, THREAD_NAME, IS_DEAMON);
        }
      }
    }
    return fixedThreadPool;
  }

  public static void shutDown() {
    if (fixedThreadPool != null) {
      fixedThreadPool.shutdownNow();
    }
    if (cachedThreadPool != null) {
      cachedThreadPool.shutdownNow();
    }
  }
}
