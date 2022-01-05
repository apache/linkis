/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.bml.common;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.linkis.bml.conf.BmlServerConfiguration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池管理器，用于获取各个线程池单例
 *
 */
public class ExecutorManager {

  /**
   * 使用多线程安全单例模式
   * 使用关键代码块synchronized,保证效率,synchronized代码块中二次检查,保证实例不被重复实例化
   */
  private static ThreadPoolExecutor uploadThreadPool;
  private static ThreadPoolExecutor updateThreadPool;
  private static ThreadPoolExecutor downloadThreadPool;
  private static final int BML_MAX_THREAD_SIZE = ((Number)BmlServerConfiguration.BML_MAX_THREAD_SIZE().getValue()).intValue();

  public ExecutorManager() {
  }

  /**
   * 获取上传任务的线程池
   *
   * @return 线程名为upload-thread-%d
   */
  public static ThreadPoolExecutor getUploadThreadPool() {
    if (uploadThreadPool != null) {
      return uploadThreadPool;
    }
    synchronized (ExecutorManager.class) {
      if (uploadThreadPool == null) {
        // 自定义线程名，方便出错时回溯
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(
            "upload-thread-%d").build();
        uploadThreadPool = new ThreadPoolExecutor(BML_MAX_THREAD_SIZE,
                                                  BML_MAX_THREAD_SIZE, 0L,
                                                  TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                                                  namedThreadFactory,
                                                  new ThreadPoolExecutor.AbortPolicy());
      }
    }

    return uploadThreadPool;
  }

  /**
   * 获取更新任务的线程池
   *
   * @return 线程名为update-thread-%d
   */
  public static ThreadPoolExecutor getUpdateThreadPool() {
    if (updateThreadPool != null) {
      return updateThreadPool;
    }
    synchronized (ExecutorManager.class) {
      if (updateThreadPool == null) {
        // 自定义线程名，方便出错时回溯
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(
            "update-thread-%d").build();
        updateThreadPool = new ThreadPoolExecutor(BML_MAX_THREAD_SIZE,
                                                  BML_MAX_THREAD_SIZE, 0L,
                                                  TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                                                  namedThreadFactory,
                                                  new ThreadPoolExecutor.AbortPolicy());
      }
    }

    return updateThreadPool;
  }

  /**
   * 获取下载任务的线程池
   *
   * @return 线程名为download-thread-%d
   */
  public static ThreadPoolExecutor getDownloadThreadPool() {
    if (downloadThreadPool != null) {
      return downloadThreadPool;
    }
    synchronized (ExecutorManager.class) {
      if (downloadThreadPool == null) {
        // 自定义线程名，方便出错时回溯
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat(
            "download-thread-%d").build();
        downloadThreadPool = new ThreadPoolExecutor(BML_MAX_THREAD_SIZE,
                                                    BML_MAX_THREAD_SIZE, 0L,
                                                  TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                                                  namedThreadFactory,
                                                  new ThreadPoolExecutor.AbortPolicy());
      }
    }

    return downloadThreadPool;
  }
}
