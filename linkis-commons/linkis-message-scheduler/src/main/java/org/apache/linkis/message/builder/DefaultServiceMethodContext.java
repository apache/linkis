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
 
package org.apache.linkis.message.builder;

import org.apache.linkis.message.context.MessageSchedulerContext;
import org.apache.linkis.message.exception.MessageWarnException;
import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.scheduler.queue.SchedulerEventState;
import scala.concurrent.duration.Duration;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.linkis.message.conf.MessageSchedulerConf.*;


public class DefaultServiceMethodContext implements ServiceMethodContext {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    private final ThreadLocal<Set<Integer>> skips = new ThreadLocal<>();

    private final ThreadLocal<Job> job = new ThreadLocal<>();

    @Override
    public void putAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    @Override
    public void putIfAbsent(String key, Object value) {
        if (!notNull(key)) {
            putAttribute(key, value);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) this.attributes.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttributeOrDefault(String key, T defaultValue) {
        return (T) this.attributes.getOrDefault(key, defaultValue);
    }

    @Override
    public String getUser() {
        return getAttribute(USER_KEY);
    }

    @Override
    public HttpServletRequest getRequest() {
        return getAttribute(REQUEST_KEY);
    }

    @Override
    public boolean notNull(String key) {
        return this.attributes.get(key) != null;
    }

    @Override
    public MessageJob publish(RequestProtocol requestProtocol) throws MessageWarnException {
        MessageSchedulerContext context = getAttribute(CONTEXT_KEY);
        return context.getPublisher().publish(requestProtocol, this);
    }

    @Override
    public void send(Object message) {
        Sender sender = getAttribute(SENDER_KEY);
        sender.send(message);
    }

    @Override
    public Object ask(Object message) {
        Sender sender = getAttribute(SENDER_KEY);
        return sender.ask(message);
    }

    @Override
    public Object ask(Object message, Duration timeout) {
        Sender sender = getAttribute(SENDER_KEY);
        return sender.ask(message, timeout);
    }

    @Override
    public Sender getSender() {
        return getAttribute(SENDER_KEY);
    }

    @Override
    public void setTimeoutPolicy(MessageJobTimeoutPolicy policy) {
        putAttribute(TIMEOUT_POLICY, policy);
    }

    @Override
    public void setResult(Object result) {
        putAttribute(RESULT_KEY, result);
    }

    @Override
    public <T> T getResult() {
        return getAttribute(RESULT_KEY);
    }

    @Override
    public boolean isInterrupted() {
        //linkis-shceduler 没有isInterrupted状态
        return SchedulerEventState.Cancelled() == this.job.get().getState();
    }

    @Override
    public boolean isCancel() {
        //linkis-shceduler只有cancel
        return SchedulerEventState.Cancelled() == this.job.get().getState();
    }

    @Override
    public boolean isSuccess() {
        return SchedulerEventState.Succeed() == this.job.get().getState();
    }

    public void setJob(Job job) {
        this.job.set(job);
    }

    public void removeJob() {
        this.job.remove();
    }

    public void setSkips(Integer... orders) {
        Set<Integer> oldOrders = skips.get();
        if (oldOrders == null) {
            Set<Integer> newOrders = new HashSet<Integer>(Arrays.asList(orders));
            skips.set(newOrders);
        } else {
            oldOrders.addAll(Arrays.asList(orders));
        }
    }

    public void removeSkips() {
        this.skips.remove();
    }

}
