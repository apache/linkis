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

import org.apache.linkis.message.context.AbstractMessageSchedulerContext;
import org.apache.linkis.message.scheduler.MethodExecuteWrapper;
import org.apache.linkis.protocol.message.RequestProtocol;
import org.apache.linkis.scheduler.executer.ExecuteRequest;
import org.apache.linkis.scheduler.queue.Job;
import org.apache.linkis.scheduler.queue.JobInfo;
import org.apache.linkis.scheduler.queue.SchedulerEventState;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;


public class DefaultMessageJob extends Job implements MessageJob {

    private RequestProtocol requestProtocol;

    private Map<String, List<MethodExecuteWrapper>> methodExecuteWrappers;

    private ServiceMethodContext smc;

    private AbstractMessageSchedulerContext context;

    //implements of MessageJob

    @Override
    public RequestProtocol getRequestProtocol() {
        return this.requestProtocol;
    }

    @Override
    public void setRequestProtocol(RequestProtocol requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

    @Override
    public Map<String, List<MethodExecuteWrapper>> getMethodExecuteWrappers() {
        return this.methodExecuteWrappers;
    }

    @Override
    public void setMethodExecuteWrappers(Map<String, List<MethodExecuteWrapper>> methodExecuteWrappers) {
        this.methodExecuteWrappers = methodExecuteWrappers;
    }

    @Override
    public ServiceMethodContext getMethodContext() {
        return this.smc;
    }

    @Override
    public void setMethodContext(ServiceMethodContext smc) {
        this.smc = smc;
    }

    @Override
    public AbstractMessageSchedulerContext getContext() {
        return this.context;
    }

    @Override
    public void setContext(AbstractMessageSchedulerContext context) {
        this.context = context;
    }

    //implements of Job

    @Override
    public void init() {
    }

    @Override
    public ExecuteRequest jobToExecuteRequest() {
        return () -> null;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public JobInfo getJobInfo() {
        return null;
    }

    @Override
    public void close() throws IOException {
    }

    // implements of Future

    // TODO: 2020/8/3 state 和blockThread的cas化

    Thread blockThread = null;


    public Thread getBlockThread() {
        return this.blockThread;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (mayInterruptIfRunning) {
            cancel();
        }
        return true;
    }

    @Override
    public Object get() throws ExecutionException, InterruptedException {
        if (!this.isCompleted()) {
            waitComplete(false, -1L);
        }
        return handleResult();
    }

    @Override
    public Object getPartial() {
        return this.getMethodContext().getResult();
    }

    public Object handleResult() throws ExecutionException {
        if (this.isSucceed()) {
            return this.getMethodContext().getResult();
        }
        // TODO: 2020/8/3  cancel逻辑加入
        throw new ExecutionException(this.getErrorResponse().t());
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        if (unit == null) unit = TimeUnit.NANOSECONDS;
        if (!this.isCompleted()
                && !SchedulerEventState.isCompleted(SchedulerEventState.apply(waitComplete(true, unit.toNanos(timeout))))) {
            String msg = "task: " +  this.requestProtocol + "time out " + timeout;
            throw new TimeoutException(msg);
        }
        return handleResult();
    }

    private int waitComplete(boolean timed, long nanos) throws InterruptedException {
        long endTime = timed ? System.nanoTime() + nanos : -1L;
        for (; ; ) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            if (this.isCompleted()) {
                return this.getState().id();
            } else if (blockThread == null)
                blockThread = Thread.currentThread();
            else if (timed) {
                nanos = endTime - System.nanoTime();
                if (nanos <= 0) {
                    return this.getState().id();
                }
                LockSupport.parkNanos(this, nanos);
            } else
                LockSupport.park(this);
        }
    }
}
