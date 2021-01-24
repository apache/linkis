package com.webank.wedatasphere.linkis.engine.flink.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.base.Predicate;

/**
 * 基于guava-retry的重试工具类
 */
public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    /**
     * @param task             要重试执行得任务
     * @param predicate        符合预期结果需要重试
     * @param fixedWaitTime    本次重试与上次重试之间的固定间隔时长
     * @param maxEachExecuTime 一次重试的最大执行的时间
     * @param attemptNumber    重试次数
     */

    public static <T> T retry(Callable<T> task, Predicate<T> predicate, long fixedWaitTime, long maxEachExecuTime,
                              TimeUnit timeUnit, int attemptNumber) {
        Retryer<T> retryer = RetryerBuilder
                .<T>newBuilder()
                // 抛出runtime异常、checked异常时都会重试，但是抛出error不会重试。
                .retryIfException()
                // 对执行结果的预期。符合预期就重试
                .retryIfResult(predicate)
                // 每次重试固定等待fixedWaitTime时间
                .withWaitStrategy(WaitStrategies.fixedWait(fixedWaitTime, timeUnit))
                // 尝试次数
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