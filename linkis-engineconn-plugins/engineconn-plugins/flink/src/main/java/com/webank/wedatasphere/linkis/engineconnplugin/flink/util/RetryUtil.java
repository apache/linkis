/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.engineconnplugin.flink.util;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A retry Util-class based on guava-retry(基于guava-retry的重试工具类)
 * Retry Tool class based on guava-retry
 */
public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    /**
     * @param task             task pending for retry(重试执行得任务)
     * @param predicate        retry predication(符合预期结果需要重试)
     * @param fixedWaitTime    time interval between retries(本次重试与上次重试之间的固定间隔时长)
     * @param maxEachExecuTime maxium time for a retry(一次重试的最大执行的时间)
     * @param attemptNumber    number of retry attempts(重试次数)
     */

    public static <T> T retry(Callable<T> task, Predicate<T> predicate, long fixedWaitTime, long maxEachExecuTime,
                              TimeUnit timeUnit, int attemptNumber) {
        Retryer<T> retryer = RetryerBuilder
                .<T>newBuilder()
                // Will retry: runtime exception; checked exception. May not retry: error (抛出runtime异常、checked异常时都会重试，但是抛出error不会重试。)
                .retryIfException()
                // if predication is met, then retry(对执行结果的预期。符合预期就重试)
                .retryIfResult(predicate)
                // fixed waiting time for retry(每次重试固定等待fixedWaitTime时间)
                .withWaitStrategy(WaitStrategies.fixedWait(fixedWaitTime, timeUnit))
                // number of retry attempts(尝试次数)
                .withStopStrategy(StopStrategies.stopAfterAttempt(attemptNumber))
                .build();
        T t = null;
        try {
            t = retryer.call(task);
        } catch (ExecutionException e) {
            logger.error("", e);
        } catch (RetryException e) {
            logger.error("", e);
        }
        return t;
    }

}