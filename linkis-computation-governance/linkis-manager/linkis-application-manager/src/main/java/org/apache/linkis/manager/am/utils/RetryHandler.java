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

package org.apache.linkis.manager.am.utils;

import java.util.function.Supplier;

public interface RetryHandler<T> {

  void setRetryNum(int retryNum);

  int getRetryNum();

  void setRetryPeriod(long retryPeriod);

  long getRetryPeriod();

  void setRetryMaxPeriod(long retryMaxPeriod);

  long getRetryMaxPeriod();

  void addRetryException(Class<? extends Throwable> t);

  Class<? extends Throwable>[] getRetryExceptions();

  boolean exceptionCanRetry(Throwable t);

  long nextInterval(int attempt);

  T retry(Supplier<T> op, String retryName);
}
