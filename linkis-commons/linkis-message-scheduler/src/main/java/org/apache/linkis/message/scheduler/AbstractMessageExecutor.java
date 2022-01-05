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
 
package org.apache.linkis.message.scheduler;

import org.apache.linkis.common.utils.JavaLog;
import org.apache.linkis.message.builder.DefaultServiceMethodContext;
import org.apache.linkis.message.builder.MessageJob;
import org.apache.linkis.message.builder.ServiceMethodContext;
import org.apache.linkis.message.exception.MessageWarnException;
import org.apache.linkis.message.parser.ImplicitMethod;
import org.apache.linkis.message.utils.MessageUtils;
import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.rpc.MessageErrorConstants;
import org.apache.linkis.scheduler.queue.Job;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public abstract class AbstractMessageExecutor extends JavaLog implements MessageExecutor {

    private Throwable t;

    private void methodErrorHandle(Throwable t) {
        if (t.getCause() != null) {
            this.t = t;
        } else {
            this.t = t;
            logger().debug("unexpected error occur");
        }
    }

    private List<MethodExecuteWrapper> getMinOrderMethodWrapper(Map<String, List<MethodExecuteWrapper>> methodWrappers) {
        //获取所有key中order最小的
        List<MethodExecuteWrapper> minOrderMethodWrapper = new ArrayList<>();
        methodWrappers.forEach((k, v) -> v.forEach(m -> {
            if (MessageUtils.orderIsMin(m, v)) minOrderMethodWrapper.add(m);
        }));
        return minOrderMethodWrapper;
    }

    private List<MethodExecuteWrapper> getMinOrderMethodWrapper(List<MethodExecuteWrapper> methodWrappers) {
        //获取单个key中order最小的，一般是一个，尾链可能有多个
        return methodWrappers.stream().filter(m -> MessageUtils.orderIsMin(m, methodWrappers)).collect(Collectors.toList());
    }

    private boolean shouldBreak(Map<String, List<MethodExecuteWrapper>> methodWrappers) {
        return methodWrappers.values().stream().allMatch(List::isEmpty);
    }

    private void cleanMethodContextThreadLocal(ServiceMethodContext methodContext) {
        if (methodContext instanceof DefaultServiceMethodContext) {
            ((DefaultServiceMethodContext) methodContext).removeJob();
            ((DefaultServiceMethodContext) methodContext).removeSkips();
        }
    }

    private void setMethodContextThreadLocal(ServiceMethodContext methodContext, MessageJob job) {
        if (methodContext instanceof DefaultServiceMethodContext && job instanceof Job) {
            ((DefaultServiceMethodContext) methodContext).setJob((Job) job);
        }
    }

    @Override
    public void run(MessageJob job) throws InterruptedException {
        Map<String, List<MethodExecuteWrapper>> methodWrappers = job.getMethodExecuteWrappers();
        Integer count = methodWrappers.values().stream().map(List::size).reduce(0, Integer::sum);
        if (count == 1) {
            runOneJob(job);
        } else {
            runMultipleJob(job);
        }
    }

    private void runMultipleJob(MessageJob job)  throws InterruptedException {
        RequestProtocol requestProtocol = job.getRequestProtocol();
        ServiceMethodContext methodContext = job.getMethodContext();
        // TODO: 2020/7/22 data structure optimization of variable methodWrappers
        Map<String, List<MethodExecuteWrapper>> methodWrappers = job.getMethodExecuteWrappers();
        Integer count = methodWrappers.values().stream().map(List::size).reduce(0, Integer::sum);
        LinkedBlockingDeque<MethodExecuteWrapper> queue = new LinkedBlockingDeque<>(16);
        CopyOnWriteArrayList<Future<?>> methodFutures = new CopyOnWriteArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(count);
        getMinOrderMethodWrapper(methodWrappers).forEach(queue::offer);
        try {
            while (!Thread.interrupted()) {
                if (shouldBreak(methodWrappers)) {
                    break;
                }
                MethodExecuteWrapper methodWrapper = queue.poll(10, TimeUnit.MILLISECONDS);
                if (methodWrapper == null) continue;
                methodWrappers.get(methodWrapper.getChainName()).remove(methodWrapper);
                Future<?> methodFuture = getExecutorService().submit(() -> {
                    Object result = null;
                    try {
                        // TODO: 2020/7/31 判断逻辑挪走
                        if (!methodWrapper.shouldSkip) {
                            //放置job状态
                            setMethodContextThreadLocal(methodContext, job);
                            Method method = methodWrapper.getMethod();
                            Object service = methodWrapper.getService();
                            info(String.format("message scheduler executor ===> service: %s,method: %s", service.getClass().getName(), method.getName()));
                            Object implicit;
                            // TODO: 2020/8/4 implicit 的结果应该复用下
                            ImplicitMethod implicitMethod = methodWrapper.getImplicitMethod();
                            if (implicitMethod != null) {
                                implicit = implicitMethod.getMethod().invoke(implicitMethod.getImplicitObject(), requestProtocol);
                            } else {
                                implicit = requestProtocol;
                            }
                            if (methodWrapper.isHasMethodContext()) {
                                if (methodWrapper.isMethodContextOnLeft()) {
                                    result = method.invoke(service, methodContext, implicit);
                                } else {
                                    result = method.invoke(service, implicit, methodContext);
                                }
                            } else {
                                result = method.invoke(service, implicit);
                            }
                            // TODO: 2020/8/5  执行完成后判断service是否有主动skip的逻辑
                        }
                    } catch (Throwable t) {
                        logger().error(String.format("method %s call failed", methodWrapper.getAlias()), t);
                        methodWrappers.forEach((k, v) -> v.forEach(m -> m.setShouldSkip(true)));
                        methodErrorHandle(t);
                    } finally {
                        if (result != null) {
                            methodContext.setResult(result);
                        }
                        //末链并发的时候，小概率可能会有重复的method被offer到queue中，但是在poll前循环就break了，无影响
                        getMinOrderMethodWrapper(methodWrappers.get(methodWrapper.getChainName())).forEach(queue::offer);
                        //移除state和skips的状态
                        cleanMethodContextThreadLocal(methodContext);
                        countDownLatch.countDown();
                    }
                });
                methodFutures.add(methodFuture);
            }
            countDownLatch.await();
        } catch (InterruptedException ie) {
            methodFutures.forEach(f -> f.cancel(true));
            throw ie;
        }
        if (this.t != null) {
            throw new MessageWarnException(MessageErrorConstants.MESSAGE_ERROR(), "method call failed", t);
        }
    }

    private void runOneJob(MessageJob job) {
        RequestProtocol requestProtocol = job.getRequestProtocol();
        ServiceMethodContext methodContext = job.getMethodContext();
        Map<String, List<MethodExecuteWrapper>> methodWrappers = job.getMethodExecuteWrappers();
        List<MethodExecuteWrapper> methodExecuteWrappers = getMinOrderMethodWrapper(methodWrappers);
        if (methodExecuteWrappers.size() == 1) {
            MethodExecuteWrapper methodWrapper = methodExecuteWrappers.get(0);
            Object result = null;
            try {
                if (!methodWrapper.shouldSkip) {
                    //放置job状态
                    setMethodContextThreadLocal(methodContext, job);
                    Method method = methodWrapper.getMethod();
                    Object service = methodWrapper.getService();
                    logger().info(String.format("message scheduler executor ===> service: %s,method: %s", service.getClass().getName(), method.getName()));
                    Object implicit;
                    ImplicitMethod implicitMethod = methodWrapper.getImplicitMethod();
                    if (implicitMethod != null) {
                        implicit = implicitMethod.getMethod().invoke(implicitMethod.getImplicitObject(), requestProtocol);
                    } else {
                        implicit = requestProtocol;
                    }
                    if (methodWrapper.isHasMethodContext()) {
                        if (methodWrapper.isMethodContextOnLeft()) {
                            result = method.invoke(service, methodContext, implicit);
                        } else {
                            result = method.invoke(service, implicit, methodContext);
                        }
                    } else {
                        result = method.invoke(service, implicit);
                    }
                }
            } catch (Throwable t) {
                logger().error(String.format("method %s call failed", methodWrapper.getAlias()), t);
                methodWrappers.forEach((k, v) -> v.forEach(m -> m.setShouldSkip(true)));
                methodErrorHandle(t);
            } finally {
                if (result != null) {
                    methodContext.setResult(result);
                }
            }
        }
        if (this.t != null) {
            throw new MessageWarnException(10000, "method call failed", t);
        }
    }

}