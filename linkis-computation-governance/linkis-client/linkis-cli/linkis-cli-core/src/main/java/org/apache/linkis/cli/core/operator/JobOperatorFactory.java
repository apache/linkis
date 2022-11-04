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

package org.apache.linkis.cli.core.operator;

import org.apache.linkis.cli.common.entity.operator.JobOperator;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.LinkisClientExecutionException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class JobOperatorFactory {
  private static Map<String, JobOperatorBuilder> builderMap = new ConcurrentHashMap<>();

  private static Map<String, JobOperator> instanceMap = new ConcurrentHashMap<>(); // for singleton
  private static Map<String, ReentrantLock> lockMap = new ConcurrentHashMap<>(); // for singleton

  public static synchronized void register(String name, JobOperatorBuilder builder)
      throws Exception {
    if (builderMap.containsKey(name)
        || lockMap.containsKey(name)
        || instanceMap.containsKey(name)) {
      throw new LinkisClientExecutionException(
          "EXE0027",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Attempting to register a duplicate jobOperator, name: " + name);
    }
    builderMap.put(name, builder);
    lockMap.put(name, new ReentrantLock());
  }

  public static synchronized void remove(String name) {
    builderMap.remove(name);
    instanceMap.remove(name);
    lockMap.remove(name);
  }

  public static JobOperator getReusable(String name) throws Exception {
    JobOperatorBuilder builder = builderMap.get(name);
    ReentrantLock lock = lockMap.get(name);
    JobOperator instance = instanceMap.get(name);
    if (lock == null || builder == null) {
      throw new LinkisClientExecutionException(
          "EXE0028",
          ErrorLevel.ERROR,
          CommonErrMsg.ExecutionInitErr,
          "Failed to get a reusable joboperator, name: " + name);
    }
    if (instance == null) {
      boolean ok = lock.tryLock(500, TimeUnit.MILLISECONDS);
      if (!ok) {
        throw new LinkisClientExecutionException(
            "EXE0028",
            ErrorLevel.ERROR,
            CommonErrMsg.ExecutionInitErr,
            "Failed to get a reusable joboperator, name: " + name);
      }
      if (instance == null) {
        instance = builder.build();
        instanceMap.put(name, instance);
      }
      lock.unlock();
    }
    return instance;
  }

  public static JobOperator getNew(String name) throws Exception {
    JobOperatorBuilder builder = builderMap.get(name);
    if (builder == null) {
      throw new Exception("TODO"); // TODO
    }
    return builder.build();
  }
}
