/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.message.scheduler;

import com.webank.wedatasphere.linkis.message.builder.MessageJob;
import com.webank.wedatasphere.linkis.message.exception.MessageWarnException;
import com.webank.wedatasphere.linkis.message.tx.TransactionManager;
import com.webank.wedatasphere.linkis.protocol.engine.EngineState;
import com.webank.wedatasphere.linkis.rpc.MessageErrorConstants;
import com.webank.wedatasphere.linkis.scheduler.executer.*;
import com.webank.wedatasphere.linkis.scheduler.queue.SchedulerEvent;

import java.io.IOException;
import java.util.concurrent.ExecutorService;


public class DefaultMessageExecutor extends AbstractMessageExecutor implements Executor {

    private final ExecutorService executorService;

    private final SchedulerEvent event;

    public DefaultMessageExecutor(SchedulerEvent event, ExecutorService executorService) {
        this.event = event;
        this.executorService = executorService;
    }

    @Override
    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public long getId() {
        return 0;
    }

    /**
     * @param executeRequest
     * @return
     */
    @Override
    public ExecuteResponse execute(ExecuteRequest executeRequest) {
        if (event instanceof MessageJob) {
            TransactionManager txManager = ((MessageJob) event).getContext().getTxManager();
            Object o = txManager.begin();
            try {
                run((MessageJob) event);
                txManager.commit(o);
                return new SuccessExecuteResponse();
            } catch (InterruptedException ie) {
                //handle InterruptedException
                logger().error("message job execution interrupted", ie);
                txManager.rollback(o);
                return new ErrorExecuteResponse("message job execution interrupted", ie);
            } catch (MessageWarnException mwe) {
                //handle method call failed
                logger().error("method call normal error return");
                txManager.rollback(o);
                return new ErrorExecuteResponse("method call failed", mwe);
            } catch (Throwable t) {
                logger().debug("unexpected error occur", t);
                txManager.rollback(o);
                return new ErrorExecuteResponse("unexpected error", t);
            }
        }
        MessageWarnException eventNotMatchError = new MessageWarnException(MessageErrorConstants.MESSAGE_ERROR()
                , "event is "
                + "not instance of MessageJob");
        return new ErrorExecuteResponse("event is not instance of MessageJob", eventNotMatchError);

    }

    @Override
    public EngineState state() {
        return null;
    }

    @Override
    public ExecutorInfo getExecutorInfo() {
        return new ExecutorInfo(0, null);
    }




    @Override
    public void close() throws IOException {

    }
}
