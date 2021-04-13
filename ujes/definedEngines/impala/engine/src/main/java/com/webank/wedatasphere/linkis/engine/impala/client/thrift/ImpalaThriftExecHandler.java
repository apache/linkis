/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.linkis.engine.impala.client.thrift;


import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecHandler;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.hive.service.cli.thrift.TCancelOperationReq;
import org.apache.hive.service.cli.thrift.TCloseOperationReq;
import org.apache.hive.service.cli.thrift.TOperationHandle;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Data
public class ImpalaThriftExecHandler extends ExecHandler<TOperationHandle> implements Future<Long> {
    private ImpalaThriftSessionHolder sessionHolder;
    private volatile boolean closed;
    private long timestamp;

    public ImpalaThriftExecHandler(ImpalaThriftSessionHolder sessionHolder, String queryId, TOperationHandle handle,
                                   ResultListener listener) {
        super(queryId, handle, listener);
        this.sessionHolder = sessionHolder;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        /*
         * 关闭
         */
        try {
            TCloseOperationReq closeReq = new TCloseOperationReq();
            closeReq.setOperationHandle(getHandler());
            sessionHolder.client().CloseOperation(closeReq);
        } catch (Exception e) {
            log.warn("Failed to close the operation. {}: {}", e.getClass().getName(), e.getMessage());
        }
        sessionHolder.close();

        closed = true;
    }

    public void cancel() {
        if (closed) {
            return;
        }
        /*
         * 取消查询
         */
        try {
            TCancelOperationReq cancelReq = new TCancelOperationReq();
            cancelReq.setOperationHandle(getHandler());
            sessionHolder.client().CancelOperation(cancelReq);
        } catch (Exception e) {
            log.warn("Failed to safely cancel the query. {}: {}.", e.getClass().getName(), e.getMessage());
        }

        close();
    }

    public TSessionHandle session() {
        return sessionHolder.session();
    }

    public ImpalaHiveServer2Service.Client client() {
        return sessionHolder.client();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        cancel();
        return true;
    }

    @Override
    public boolean isCancelled() {
        return closed;
    }

    @Override
    public boolean isDone() {
        return closed;
    }

    @Override
    public Long get() throws InterruptedException, ExecutionException {
        while (true) {
            if (closed) {
                return System.currentTimeMillis() - timestamp;
            }
            Thread.sleep(1000);
        }
    }

    @Override
    public Long get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        timeout = unit.toMillis(timeout);
        long step = timeout > 1000 ? 1000 : timeout;
        while (timeout > 0) {
            if (closed) {
                return System.currentTimeMillis() - timestamp;
            }
            Thread.sleep(step);
            timeout -= step;
        }
        throw new TimeoutException();
    }
}
