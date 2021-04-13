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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaClient;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaResultSet;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaTransportFactory;
import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.ExceptionCode;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecProgress;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecStatus;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecSummary;
import com.webank.wedatasphere.linkis.engine.impala.client.util.Constant;
import com.webank.wedatasphere.linkis.engine.impala.client.util.ImpalaThriftUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.hive.service.cli.thrift.TExecuteStatementReq;
import org.apache.hive.service.cli.thrift.TExecuteStatementResp;
import org.apache.hive.service.cli.thrift.TGetOperationStatusReq;
import org.apache.hive.service.cli.thrift.TGetOperationStatusResp;
import org.apache.hive.service.cli.thrift.TOperationHandle;
import org.apache.hive.service.cli.thrift.TOperationState;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.impala.thrift.TExecProgress;
import org.apache.impala.thrift.TExecSummary;
import org.apache.impala.thrift.TGetExecSummaryReq;
import org.apache.impala.thrift.TGetExecSummaryResp;
import org.apache.thrift.TException;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基于Thrift协议并以HiveServer2进行通信的Impala客户端
 *
 * @author dingqihuang
 * @version Sep 20, 2019
 */
@Slf4j
public class ImpalaThriftClientOnHiveServer2 extends TimerTask implements ImpalaClient {


    private final Map<String, String> queryOptions;
    private final Map<String, ImpalaThriftExecHandler> handlers;
    private final int parallelLimit;
    private final int heartBeatsInMilliSecond;
    private final long queryTimeout;
    final private ImpalaThriftTransportFactory transport;
    /**
     * 每次拉取的数据大小
     */
    private int batchSize = Constant.DEFAULT_BATCH_SIZE;
    /**
     * 允许闲置时间
     */
    private volatile ImpalaThriftClientHolder clientHolder;
    private volatile boolean closed;

    private Timer timer;
    private AtomicInteger counter = new AtomicInteger(0);

    public ImpalaThriftClientOnHiveServer2(ImpalaTransportFactory<?> transport, int parallelLimit,
                                           int heartBeatsInSecond, int queryTimeout)
            throws TransportException {

        if (!(transport instanceof ImpalaThriftTransportFactory)) {
            throw new TransportException(
                    "Expected a ImpalaThriftTransport, but got a(n) " + transport.getClass().getName());
        }

        this.transport = (ImpalaThriftTransportFactory) transport;
        this.handlers = Maps.newConcurrentMap();
        this.queryOptions = Maps.newConcurrentMap();
        this.parallelLimit = parallelLimit;
        this.heartBeatsInMilliSecond = heartBeatsInSecond * 1000;
        this.queryTimeout = queryTimeout * 1000;
        this.closed = false;

        this.clientHolder = null;

        /*
         * Connect to server
         */
        this.connect();
    }


    @Override
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Connect to server
     *
     * @throws TransportException failed to create the session
     */
    private void connect() throws TransportException {
        if (clientHolder != null) {
            clientHolder.close();
            try {

            } catch (Exception e) {
                log.warn("Failed to safely close the client. {}: {}", e.getClass().getName(), e.getMessage());
            }
        }
        try {
            clientHolder = new ImpalaThriftClientHolder(transport, parallelLimit);
        } catch (Exception e) {
            throw TransportException.of(ExceptionCode.LoginError, e);
        }

        if (timer == null) {
            timer = new Timer();
            timer.schedule(this, heartBeatsInMilliSecond, heartBeatsInMilliSecond);
        }
    }

    /**
     * Close the client
     */
    @Override
    public void close() throws Exception {
        this.closed = true;

        clientHolder.close();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private ImpalaThriftExecHandler submit(String sql, ResultListener resultListener) throws TException,
            TransportException, SubmitException, InterruptedException {

        ImpalaThriftSessionHolder sessionHolder = clientHolder.open();

        if (sessionHolder == null) {
            throw SubmitException.of(ExceptionCode.ParallelLimitError, "timeout for waiting available connection.");
        }

        TSessionHandle session = sessionHolder.session();
        ImpalaHiveServer2Service.Client client = sessionHolder.client();

        TExecuteStatementReq req = new TExecuteStatementReq(session, sql);
        req.setRunAsync(false);
        req.setConfOverlay(queryOptions);

        TExecuteStatementResp res = null;
        for (int i = 0; i < 2; ++i) {
            try {
                res = client.ExecuteStatement(req);
                break;
            } catch (Throwable t) {
                System.out.println(sql + " " + t.getMessage());
                log.warn("Failed to submit execute statement. {}: {}", t.getClass().getName(), t.getMessage());
            }
        }

        if (res == null) {
            /*
             * 尝试提交失败，重连并提交
             */
            this.connect();
            session = sessionHolder.session();
            client = sessionHolder.client();
            req.setSessionHandle(session);
            res = client.ExecuteStatement(req);
        }

        ImpalaThriftUtil.checkStatus(res.getStatus(), resultListener);

        /*
         * 处理返回结果
         */
        TOperationHandle operation = res.getOperationHandle();
        if (operation == null) {
            throw SubmitException.of(ExceptionCode.CommunicateError);
        }

        String queryId = ImpalaThriftUtil.printUniqueId(operation.getOperationId().getGuid());
        return new ImpalaThriftExecHandler(sessionHolder, queryId, operation, resultListener);
    }

    private boolean progress(ImpalaThriftExecHandler handler) {
        ExecSummary summary = null;
        try {
            summary = getExecSummary(handler);

            ResultListener resultListener = handler.getResultListener();
            ExecStatus status = summary.getStatus();
            if (status.isActive()) {
                /*
                 * 正在执行，汇报进度
                 */
                if (resultListener != null && summary.getProgress() != null) {
                    resultListener.progress(summary.getProgress());
                }

                // 超时检测
                if (queryTimeout > 0
                        && (System.currentTimeMillis() - handler.getTimestamp()) > queryTimeout) {
                    return false;
                }
                return true;
            }

            log.info("summary status: {}", status.getCode());

            if (status.hasError()) {
                if (resultListener != null) {
                    resultListener.error(summary.getStatus());
                }
            } else if (handler.getHandler().isHasResultSet()) {
                fetchResult(handler);
            }
            return false;
        } catch (TransportException e) {
            if (handler.markError() >= Constant.DEFAULT_ERROR_INDURANCE) {
                log.warn("Progress failed when fetching summary.", e);
                return false;
            }

            log.warn("Progress error when fetching summary. {}: {}", e.getClass().getName(), e.getMessage());

            // 超时检测
            if (queryTimeout > 0
                    && System.currentTimeMillis() - handler.getTimestamp() > queryTimeout) {
                return false;
            }
            return true;
        }
    }


    @Override
    public void execute(String sql, ResultListener resultListener) throws TransportException, SubmitException {
        if (closed) {
            throw SubmitException.of(ExceptionCode.ClosedError);
        }
        ImpalaThriftExecHandler handler = null;
        try {
            handler = submit(sql, resultListener);

            /*
             * 轮询
             */
            while (progress(handler)) {
                Thread.sleep(heartBeatsInMilliSecond);
            }
        } catch (InterruptedException e) {
            /*
             * 进程被kill
             */
            log.warn("The query was interrupted.", e);
        } catch (TException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        } finally {
            if (handler != null) {
                handler.close();
            }
        }
    }

    @Override
    public String executeAsync(String sql, ResultListener resultListener) throws TransportException, SubmitException {
        if (closed) {
            throw SubmitException.of(ExceptionCode.ClosedError);
        }

        ImpalaThriftExecHandler handler = null;
        try {
            handler = submit(sql, resultListener);

            handlers.put(handler.getQueryId(), handler);
            return handler.getQueryId();
        } catch (TException | InterruptedException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }
    }

    @Override
    public Future<Long> executeWithFuture(String sql, ResultListener resultListener) throws TransportException,
            SubmitException {
        if (closed) {
            throw SubmitException.of(ExceptionCode.ClosedError);
        }

        ImpalaThriftExecHandler handler = null;
        try {
            handler = submit(sql, resultListener);

            handlers.put(handler.getQueryId(), handler);
            return handler;
        } catch (TException | InterruptedException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }
    }

    @Override
    public void cancel(String queryId) throws TransportException {
        cancel(handlers.get(queryId));
    }

    /**
     * 取消已有查询
     *
     * @param handler 查询句柄
     * @throws TransportException
     */
    private void cancel(ImpalaThriftExecHandler handler) throws TransportException {
        if (handler == null) {
            return;
        }
        handler.cancel();
    }

    @Override
    public ExecSummary getExecSummary(String queryId) throws TransportException {
        return getExecSummary(handlers.get(queryId));
    }

    /**
     * 获取查询进度
     *
     * @param handler 查询句柄
     * @return 查询任务进度信息
     * @throws TransportException 与服务器连接失败
     */
    private ExecSummary getExecSummary(ImpalaThriftExecHandler handler) throws TransportException {
        if (handler == null) {
            return null;
        }
        try {
            TOperationHandle operation = handler.getHandler();
            /*
             * 先获取运行状态
             */
            ExecStatus status = getExecStatus(handler);

            if (status == null) {
                return null;
            }

            ExecProgress progress = null;
            int nodeNum = -1;

            /*
             * 状态有效，获取进度信息
             */
            if (status.isActive()) {
                /*
                 * 查询summary
                 */
                TGetExecSummaryReq getExecSummaryReq = new TGetExecSummaryReq();
                getExecSummaryReq.setOperationHandle(operation);
                getExecSummaryReq.setSessionHandle(handler.session());

                TGetExecSummaryResp execSummaryResp = handler.client().GetExecSummary(getExecSummaryReq);

                try {
                    ImpalaThriftUtil.checkStatus(execSummaryResp.getStatus());
                    TExecSummary summary = execSummaryResp.getSummary();
                    nodeNum = summary.getNodesSize();
                    if (handler.isQueued()) {
                        progress = new ExecProgress(-1, -1);
                    } else if (summary.isIs_queued()) {
                        handler.setQueued(true);
                        ResultListener resultListener = handler.getResultListener();
                        if (resultListener != null) {
                            resultListener.message(Lists.newArrayList(summary.getQueued_reason()));
                        }
                        progress = new ExecProgress(-1, -1);
                    } else {
                        TExecProgress p = summary.getProgress();
                        if (p != null) {
                            progress = new ExecProgress(p.total_scan_ranges, p.num_completed_scan_ranges);
                        }
                    }
                } catch (Exception e) {
                    progress = new ExecProgress(-1, -1);
                }
            }

            return new ExecSummary(status, progress, nodeNum);

        } catch (TException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }
    }

    @Override
    public ExecProgress getExecProgress(String queryId) throws TransportException {
        ExecSummary summary = getExecSummary(queryId);
        if (summary != null) {
            return summary.getProgress();
        }
        return null;
    }

    @Override
    public ExecStatus getExecStatus(String queryId) throws TransportException {
        ImpalaThriftExecHandler handler = handlers.get(queryId);
        if (handler != null) {
            return getExecStatus(handler);
        }
        return null;
    }

    private ExecStatus getExecStatus(ImpalaThriftExecHandler handler) throws TransportException {
        try {
            /*
             * 查询执行状态
             */
            TGetOperationStatusReq statusReq = new TGetOperationStatusReq(handler.getHandler());
            TGetOperationStatusResp statusResp = handler.client().GetOperationStatus(statusReq);

            ImpalaThriftUtil.checkStatus(statusResp.getStatus());

            TOperationState operationState = statusResp.getOperationState();

            return new ExecStatus(operationState.getValue(), operationState.name(), statusResp.getErrorMessage());
        } catch (TException | SubmitException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }
    }

    @Override
    public void setRequestPool(String poolName) throws TransportException {
        if (StringUtils.isBlank(poolName)) {
            unsetQueryOption(Constant.REQUEST_POOL);
        } else {
            setQueryOption(Constant.REQUEST_POOL, poolName);
        }

    }

    @Override
    public void setQueryOption(String key, String value) throws TransportException {
        if (StringUtils.isNotBlank(value)) {
            queryOptions.put(key, value);
        }
    }

    @Override
    public void unsetQueryOption(String key) throws TransportException {
        queryOptions.remove(key);
    }

    private void fetchResult(ImpalaThriftExecHandler handler) {
        if (handler != null) {
            ImpalaResultSet rs = new ImpalaThriftResultSetV7(handler.client(), handler.getHandler(), batchSize);
            ResultListener resultListener = handler.getResultListener();
            if (resultListener != null) {
                resultListener.success(rs);
            }

            /*
             * 读取结果结束，关闭
             */
            try {
                rs.close();
            } catch (Exception e) {
                log.error("Failed to safely close the result set.", e);
            }
        }
    }

    /**
     * 心跳
     */
    @Override
    public void run() {
        log.debug("Impala client heart beats.");

        Iterator<Map.Entry<String, ImpalaThriftExecHandler>> iterator = handlers.entrySet().iterator();
        while (iterator.hasNext()) {
            ImpalaThriftExecHandler handler = iterator.next().getValue();
            if (handler.isClosed()) {
                iterator.remove();
                continue;
            }
            if (!progress(handler)) {
                handler.close();
                iterator.remove();
            }
        }

        clientHolder.clean();
    }

    @Override
    public int getExecutionCount() {
        return clientHolder.activeCount();
    }

    @Override
    public Map<String, String> getQueryOptions() throws TransportException {
        return ImmutableMap.copyOf(queryOptions);
    }
}
