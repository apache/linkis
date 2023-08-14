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

import org.apache.linkis.common.exception.FatalException;

import org.apache.commons.lang3.ClassUtils;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRetryHandler<T> implements RetryHandler<T> {
  private static final Logger logger = LoggerFactory.getLogger(DefaultRetryHandler.class);

  private int retryNum = 2;
  private long period = 100L;
  private long maxPeriod = 1000L;
  private ArrayList<Class<? extends Throwable>> retryExceptions = new ArrayList<>();

  @Override
  public void setRetryNum(int retryNum) {
    this.retryNum = retryNum;
  }

  @Override
  public int getRetryNum() {
    return retryNum;
  }

  @Override
  public void setRetryPeriod(long retryPeriod) {
    this.period = retryPeriod;
  }

  @Override
  public long getRetryPeriod() {
    return period;
  }

  @Override
  public void setRetryMaxPeriod(long retryMaxPeriod) {
    this.maxPeriod = retryMaxPeriod;
  }

  @Override
  public long getRetryMaxPeriod() {
    return maxPeriod;
  }

  @Override
  public void addRetryException(Class<? extends Throwable> t) {
    retryExceptions.add(t);
  }

  @Override
  public Class<? extends Throwable>[] getRetryExceptions() {
    return retryExceptions.toArray(new Class[retryExceptions.size()]);
  }

  @Override
  public boolean exceptionCanRetry(Throwable t) {
    return !(t instanceof FatalException)
        && retryExceptions.stream().anyMatch(c -> ClassUtils.isAssignable(t.getClass(), c));
  }

  @Override
  public long nextInterval(int attempt) {
    long interval = (long) (this.period * Math.pow(1.5, attempt - 1));
    if (interval > this.maxPeriod) return this.maxPeriod;
    else return interval;
  }

  @Override
  public T retry(Supplier<T> op, String retryName) {
    if (retryExceptions.isEmpty() || retryNum <= 1) return op.get();
    int retry = 0;
    T result = null;
    while (retry < retryNum && result == null) {
      try {
        return op.get();
      } catch (Throwable t) {
        retry += 1;
        if (retry >= retryNum) throw t;
        else if (exceptionCanRetry(t)) {
          long retryInterval = nextInterval(retry);
          logger.info(
              retryName
                  + " failed with "
                  + t.getClass().getName()
                  + ", wait "
                  + retryInterval
                  + "ms for next retry. Retried "
                  + retry++
                  + " times...");
          try {
            Thread.sleep(retryInterval);
          } catch (InterruptedException e) {
          }
        } else throw t;
      }
    }
    return result;
  }
}
