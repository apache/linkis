package com.webank.wedatasphere.linkis.engine.impala.client.thrift;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.apache.hive.service.cli.thrift.TBinaryColumn;
import org.apache.hive.service.cli.thrift.TBoolColumn;
import org.apache.hive.service.cli.thrift.TByteColumn;
import org.apache.hive.service.cli.thrift.TCancelOperationReq;
import org.apache.hive.service.cli.thrift.TCloseOperationReq;
import org.apache.hive.service.cli.thrift.TCloseSessionReq;
import org.apache.hive.service.cli.thrift.TColumn;
import org.apache.hive.service.cli.thrift.TColumnDesc;
import org.apache.hive.service.cli.thrift.TDoubleColumn;
import org.apache.hive.service.cli.thrift.TExecuteStatementReq;
import org.apache.hive.service.cli.thrift.TExecuteStatementResp;
import org.apache.hive.service.cli.thrift.TFetchResultsReq;
import org.apache.hive.service.cli.thrift.TFetchResultsResp;
import org.apache.hive.service.cli.thrift.TGetOperationStatusReq;
import org.apache.hive.service.cli.thrift.TGetOperationStatusResp;
import org.apache.hive.service.cli.thrift.TGetResultSetMetadataReq;
import org.apache.hive.service.cli.thrift.TGetResultSetMetadataResp;
import org.apache.hive.service.cli.thrift.TI16Column;
import org.apache.hive.service.cli.thrift.TI32Column;
import org.apache.hive.service.cli.thrift.TI64Column;
import org.apache.hive.service.cli.thrift.TOpenSessionReq;
import org.apache.hive.service.cli.thrift.TOpenSessionResp;
import org.apache.hive.service.cli.thrift.TOperationHandle;
import org.apache.hive.service.cli.thrift.TOperationState;
import org.apache.hive.service.cli.thrift.TProtocolVersion;
import org.apache.hive.service.cli.thrift.TRowSet;
import org.apache.hive.service.cli.thrift.TSessionHandle;
import org.apache.hive.service.cli.thrift.TStatus;
import org.apache.hive.service.cli.thrift.TStatusCode;
import org.apache.hive.service.cli.thrift.TStringColumn;
import org.apache.hive.service.cli.thrift.TTableSchema;
import org.apache.impala.thrift.ImpalaHiveServer2Service;
import org.apache.impala.thrift.TExecProgress;
import org.apache.impala.thrift.TExecSummary;
import org.apache.impala.thrift.TGetExecSummaryReq;
import org.apache.impala.thrift.TGetExecSummaryResp;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransport;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaClient;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaResultSet;
import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaTransport;
import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.ExceptionCode;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.SubmitException;
import com.webank.wedatasphere.linkis.engine.impala.client.exception.TransportException;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecHandler;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecProgress;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecStatus;
import com.webank.wedatasphere.linkis.engine.impala.client.protocol.ExecSummary;
import com.webank.wedatasphere.linkis.engine.impala.client.util.Constant;

import lombok.extern.slf4j.Slf4j;

/**
 * 基于Thrift协议并以HiveServer2进行通信的Impala客户端
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */
@Slf4j
public class ImpalaThriftClientOnHiveServer2 extends TimerTask implements ImpalaClient {

    /**
     * 每次拉取的数据大小
     */
    private int batchSize = Constant.DEFAULT_BATCH_SIZE;

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    private ImpalaThriftTransport transport;
    private TTransport currentTransport;
    private ImpalaHiveServer2Service.Client client;
    private TSessionHandle session;
    private final Map<String, String> queryOptions;
    private final Map<String, ExecHandler> handlers;
    private final int parallelLimit;
    private final int heartBeatsInMilliSecond;
    private volatile boolean closed;
    private int parallel;
    private Object mutex = new Object();

    private Timer timer;

    public ImpalaThriftClientOnHiveServer2(ImpalaTransport<?> transport, int parallelLimit, int heartBeatsInSecond)
            throws TransportException {

        if (!(transport instanceof ImpalaThriftTransport)) {
            throw new TransportException(
                    "Expected a ImpalaThriftTransport, but got a(n) " + transport.getClass().getName());
        }

        this.transport = (ImpalaThriftTransport) transport;
        this.handlers = Maps.newConcurrentMap();
        this.queryOptions = Maps.newConcurrentMap();
        this.parallelLimit = parallelLimit;
        this.parallel = 0;
        this.heartBeatsInMilliSecond = heartBeatsInSecond * 1000;
        this.closed = false;

        this.client = null;
        this.session = null;

        /*
         * Connect to server
         */
        this.connect();
    }

    /**
     * Connect to server
     * 
     * @throws TransportException failed to create the session
     */
    private void connect() throws TransportException {
        if (currentTransport != null) {
            currentTransport.close();
            session = null;
        }
        try {
            currentTransport = transport.getTransport();
            client = new ImpalaHiveServer2Service.Client(new TBinaryProtocol(currentTransport));
            TOpenSessionReq openReq = new TOpenSessionReq();
            openReq.setClient_protocol(TProtocolVersion.HIVE_CLI_SERVICE_PROTOCOL_V7);
            TOpenSessionResp openResp = client.OpenSession(openReq);
            session = openResp.getSessionHandle();
        } catch (Exception e) {
            throw TransportException.of(ExceptionCode.LoginError, e);
        }

        if (session == null || !session.isSetSessionId()) {
            throw TransportException.of(ExceptionCode.LoginError);
        }

    }

    /**
     * Close the client
     */
    @Override
    public void close() throws Exception {
        this.closed = true;

        if (timer != null) {
            timer.cancel();
        }

        for (ExecHandler handler : handlers.values()) {
            cancel(handler);
        }

        if (session != null && session.isSetSessionId()) {
            TCloseSessionReq closeConnectionReq = new TCloseSessionReq(session);
            client.CloseSession(closeConnectionReq);
        }

        transport.close();
    }

    /**
     * 处理通信返回结果的状态
     * 
     * @param status
     * @param resultListener  用于接收错误信息
     * @param messageListener 用于接收警告信息
     * @throws SubmitException
     */
    private void checkStatus(TStatus status, ResultListener resultListener) throws SubmitException {
        switch (status.getStatusCode()) {

        case STILL_EXECUTING_STATUS:
            throw SubmitException.of(ExceptionCode.StillRunningError);
        case ERROR_STATUS:
            throw SubmitException.of(ExceptionCode.ExecutionError, status.getErrorMessage());
        case INVALID_HANDLE_STATUS:
            throw SubmitException.of(ExceptionCode.InvalidHandleError);
        case SUCCESS_WITH_INFO_STATUS:
            if (resultListener != null) {
                resultListener.message(status.getInfoMessages());
            }
            break;
        case SUCCESS_STATUS:
        }
    }

    /**
     * 并行锁
     * 
     * @param startHeartBeat true - 尝试启动查询进度轮询进程
     * @throws SubmitException 超过客户端并行限制
     */
    private void lock(boolean startHeartBeat) throws SubmitException {
        synchronized (mutex) {
            if (parallelLimit <= parallel) {
                throw SubmitException.of(ExceptionCode.ParallelLimitError);
            }

            ++parallel;
            if (startHeartBeat && timer == null) {
                timer = new Timer();
                timer.schedule(this, heartBeatsInMilliSecond, heartBeatsInMilliSecond);
            }
        }
    }

    /**
     * 释放并行锁
     * 
     * @throws SubmitException
     */
    private void release() {
        synchronized (mutex) {
            --parallel;

            if (timer != null && handlers.isEmpty()) {
                timer.cancel();
                timer = null;
            }
        }
    }

    /**
     * 删除handler并释放并行锁
     * 
     * @throws SubmitException
     */
    private void release(String queryId) {
        if (queryId == null) {
            return;
        }

        ExecHandler handler = handlers.remove(queryId);
        if (handler != null) {

            /*
             * 关闭任务
             */
            close(handler);

            release();
        }
    }

    /**
     * 提交查询
     * 
     * @param sql   查询语句
     * @param async true - 异步提交
     * @return 服务器返回结果
     * @throws TException         查询出错
     * @throws TransportException 无法连接到服务器
     */
    private TExecuteStatementResp submitExecuteStatment(String sql, boolean async)
            throws TException, TransportException {
        TExecuteStatementReq execReq = new TExecuteStatementReq(session, sql);
        execReq.setRunAsync(async);
        execReq.setConfOverlay(queryOptions);
        try {
            return client.ExecuteStatement(execReq);
        } catch (TException e) {

            /*
             * 连接失效，尝试重新连接
             */
            this.connect();
            execReq.setSessionHandle(session);
            return client.ExecuteStatement(execReq);
        }
    }

    @Override
    public void execute(String sql, ResultListener resultListener) throws TransportException, SubmitException {
        if (closed) {
            throw SubmitException.of(ExceptionCode.ClosedError);
        }

        lock(false);
        ExecHandler handler = null;
        try {
            /*
             * 提交查询请求
             */
            TExecuteStatementResp execResp = submitExecuteStatment(sql, false);

            /*
             * 检查通讯结果
             */
            checkStatus(execResp.getStatus(), resultListener);

            /*
             * 处理返回结果
             */
            TOperationHandle operation = execResp.getOperationHandle();
            if (operation == null) {
                throw SubmitException.of(ExceptionCode.CommunicateError);
            }

            /*
             * 轮询
             */
            ExecSummary summary = null;
            handler = new ExecHandler(null, operation, resultListener, 0, false);
            while (true) {
                Thread.sleep(heartBeatsInMilliSecond);
                summary = getExecSummary(handler);
                if (summary == null || !summary.getStatus().isActive()) {
                    break;
                }
                if (resultListener != null) {
                    resultListener.progress(summary.getProgress());
                }
                throw new InterruptedException();
            }

            if (summary == null) {
                throw SubmitException.of(ExceptionCode.CommunicateError);
            }

            if (summary.getStatus().hasError()) {
                if (resultListener != null) {
                    resultListener.error(summary.getStatus());
                }
            } else {
                fetchResult(null, operation, resultListener);
            }

        } catch (InterruptedException e) {
            /*
             * 进程被kill
             */
            log.warn("The query was interrupted.", e);
        } catch (TException e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        } finally {
            /*
             * 同步执行，完毕释放并行信号
             */
            close(handler);
            release();
        }
    }

    @Override
    public String executeAsync(String sql, ResultListener resultListener) throws TransportException, SubmitException {
        if (closed) {
            throw SubmitException.of(ExceptionCode.ClosedError);
        }

        lock(true);

        try {
            /*
             * 提交查询请求
             */
            TExecuteStatementResp execResp = submitExecuteStatment(sql, true);

            /*
             * 检查通讯结果
             */
            checkStatus(execResp.getStatus(), resultListener);

            /*
             * 处理返回结果
             */
            TOperationHandle operation = execResp.getOperationHandle();
            if (operation == null) {
                throw SubmitException.of(ExceptionCode.CommunicateError);
            }

            String queryId = printUniqueId(operation.getOperationId().getGuid());
            handlers.put(queryId, new ExecHandler(queryId, operation, resultListener, 0, false));
            return queryId;
        } catch (TException e) {
            /*
             * 异步执行，只在报错时释放并行信号
             */
            release();
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
    private void cancel(ExecHandler handler) throws TransportException {
        if (handler == null) {
            return;
        }
        /*
         * 取消查询
         */
        try {
            TCancelOperationReq cancelReq = new TCancelOperationReq();
            cancelReq.setOperationHandle((TOperationHandle) handler.getHandler());
            client.CancelOperation(cancelReq);
        } catch (Exception e) {
            throw TransportException.of(ExceptionCode.CommunicateError, e);
        }

        /*
         * 释放任务
         */
        release(handler.getQueryId());
    }

    /**
     * 关闭已有查询
     * 
     * @param handler 查询句柄
     */
    private void close(ExecHandler handler) {
        if (handler == null) {
            return;
        }
        close((TOperationHandle) handler.getHandler());
    }

    private void close(TOperationHandle operation) {
        if (operation == null) {
            return;
        }
        /*
         * 关闭
         */
        try {
            TCloseOperationReq closeReq = new TCloseOperationReq();
            closeReq.setOperationHandle(operation);
            client.CloseOperation(closeReq);
        } catch (Exception e) {
            log.warn("Failed to close the query.", e);
        }
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
    private ExecSummary getExecSummary(ExecHandler handler) throws TransportException {
        if (handler == null) {
            return null;
        }
        try {
            TOperationHandle operation = (TOperationHandle) handler.getHandler();
            /*
             * 先获取运行状态
             */
            ExecStatus status = getExecStatus(operation);

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
                getExecSummaryReq.setSessionHandle(session);

                TGetExecSummaryResp execSummaryResp = client.GetExecSummary(getExecSummaryReq);
                checkStatus(execSummaryResp.getStatus(), null);

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

            }

            return new ExecSummary(status, progress, nodeNum);

        } catch (TException | SubmitException e) {
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
        ExecHandler handler = handlers.get(queryId);
        if (handler != null) {
            return getExecStatus((TOperationHandle) handler.getHandler());
        }
        return null;
    }

    private ExecStatus getExecStatus(TOperationHandle operation) throws TransportException {
        try {
            /*
             * 查询执行状态
             */
            TGetOperationStatusReq statusReq = new TGetOperationStatusReq(operation);
            TGetOperationStatusResp statusResp = client.GetOperationStatus(statusReq);

            checkStatus(statusResp.getStatus(), null);

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

    private void fetchResult(ExecHandler handler) {
        if (handler != null) {
            fetchResult(handler.getQueryId(), (TOperationHandle) handler.getHandler(), handler.getResultListener());
        }
    }

    private void fetchResult(String queryId, TOperationHandle operation, ResultListener resultListener) {
        ResultSet rs = new ResultSet(client, operation, batchSize);
        if (resultListener != null) {
            resultListener.success(rs);
        }

        /*
         * 读取结果结束，关闭
         */
        rs.close();
        if (StringUtils.isNotBlank(queryId)) {
            this.release(queryId);
        } else {
            close(operation);
        }

    }

    private static final byte[] bitMask = { (byte) 0x01, (byte) 0x02, (byte) 0x04, (byte) 0x08, (byte) 0x10,
            (byte) 0x20, (byte) 0x40, (byte) 0x80 };
    private static final char[] hexCode = "0123456789abcdef".toCharArray();

    /*
     * impala 输出id的格式
     */
    public static String printUniqueId(byte[] b) {
        StringBuilder sb = new StringBuilder(":");
        for (int i = 0; i < 8; ++i) {
            sb.append(hexCode[(b[15 - i] >> 4) & 0xF]);
            sb.append(hexCode[(b[15 - i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] & 0xF)]);
            sb.insert(0, hexCode[(b[i] >> 4) & 0xF]);
        }
        return sb.toString();
    }

    /**
     * 结果集类
     * 
     * @author dingqihuang
     *
     * @version Sep 20, 2019
     *
     */
    protected class ResultSet implements ImpalaResultSet {

        private ImpalaHiveServer2Service.Client client;
        private TOperationHandle operation;
        private TFetchResultsReq fetchReq;

        private int index;
        private int size;
        private boolean closed;

        /*
         * 默认远端有数据
         */
        private boolean hasMoreRow = true;

        /*
         * 保存数据
         */
        private Iterator<?>[] iterators = null;
        private byte[][] nulls = null;
        private Object[] values = null;
        private Class<?>[] types = null;
        private List<String> columns = null;

        private ResultSet(ImpalaHiveServer2Service.Client client, TOperationHandle operation, int batchSize) {
            this.client = client;
            this.operation = operation;

            this.index = -1;
            this.size = 0;
            this.fetchReq = new TFetchResultsReq();
            this.fetchReq.setMaxRows(100);
            this.fetchReq.setOperationHandle(operation);
        }

        /**
         * 解析字段
         * 
         * @param column 字段
         * @param index  顺序
         */
        private void unpackColumn(TColumn column, int index) {
            Iterator<?> result = null;
            byte[] nls = null;
            Class<?> cls = null;
            switch (column.getSetField()) {
            case BINARY_VAL:
                TBinaryColumn binVal = column.getBinaryVal();
                result = binVal.getValuesIterator();
                nls = binVal.getNulls();
                cls = ByteBuffer.class;
                break;
            case BOOL_VAL:
                TBoolColumn boolVal = column.getBoolVal();
                result = boolVal.getValuesIterator();
                nls = boolVal.getNulls();
                cls = Boolean.class;
                break;
            case BYTE_VAL:
                TByteColumn byteVal = column.getByteVal();
                result = byteVal.getValuesIterator();
                nls = byteVal.getNulls();
                cls = Byte.class;
                break;
            case DOUBLE_VAL:
                TDoubleColumn doubleVal = column.getDoubleVal();
                result = doubleVal.getValuesIterator();
                nls = doubleVal.getNulls();
                cls = Double.class;
                break;
            case I16_VAL:
                TI16Column i16Val = column.getI16Val();
                result = i16Val.getValuesIterator();
                nls = i16Val.getNulls();
                cls = Short.class;
                break;
            case I32_VAL:
                TI32Column i32Val = column.getI32Val();
                result = i32Val.getValuesIterator();
                nls = i32Val.getNulls();
                cls = Integer.class;
                break;
            case I64_VAL:
                TI64Column i64Val = column.getI64Val();
                result = i64Val.getValuesIterator();
                nls = i64Val.getNulls();
                cls = Long.class;
                break;
            case STRING_VAL:
                TStringColumn stringVal = column.getStringVal();
                result = stringVal.getValuesIterator();
                nls = stringVal.getNulls();
                cls = String.class;
                break;
            }
            iterators[index] = result;
            nulls[index] = nls;
            types[index] = cls;
        }

        /**
         * 从远端请求数据
         */
        private void fetch() {
            try {
                /*
                 * 初始化
                 */
                hasMoreRow = false;
                iterators = null;
                nulls = null;
                values = null;
                types = null;

                /*
                 * 请求数据
                 */
                TFetchResultsResp resp = client.FetchResults(fetchReq);
                TStatus status = resp.getStatus();
                /*
                 * 状态异常, 退出
                 */
                if (status.getStatusCode().getValue() > TStatusCode.SUCCESS_WITH_INFO_STATUS.getValue()) {
                    throw new RuntimeException("Failed to fetch result(s). " + status.toString());
                }

                /*
                 * 远端是否还有数据
                 */
                hasMoreRow = resp.isHasMoreRows();

                /*
                 * 转换返回结果
                 */
                TRowSet result = resp.getResults();
                List<TColumn> list = result.getColumns();
                if (list != null) {
                    index = -1;

                    /*
                     * 初始化本地缓冲区
                     */
                    if (iterators == null) {
                        size = list.size();
                        iterators = new Iterator[size];
                        nulls = new byte[size][];
                        values = new Object[size];
                        types = new Class[size];
                    }

                    /*
                     * 填充缓冲区
                     */
                    int i = 0;
                    for (TColumn item : list) {
                        unpackColumn(item, i);
                        ++i;
                    }
                }
            } catch (TException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean next() {
            if (closed) {
                return false;
            }

            /*
             * 本地没有数据，且远端有数据，则请求新数据
             */
            if ((iterators == null || !iterators[0].hasNext()) && hasMoreRow) {
                fetch();
            }

            /*
             * 读取本地数据
             */
            if (iterators != null && iterators[0].hasNext()) {
                for (int i = 0; i < iterators.length; ++i) {
                    values[i] = iterators[i].next();
                }
                ++index;
                return true;
            }

            /*
             * 数据读取完毕
             */
            close();
            return false;
        }

        private boolean isNull(int columnIndex) {
            return (nulls[columnIndex][index / 8] & bitMask[index % 8]) != 0;
        }

        @Override
        public Object[] getValues() {
            Object[] result = new Object[values.length];
            for (int i = 0; i < result.length; ++i) {
                result[i] = isNull(i) ? null : values[i];
            }
            return result;
        }

        @Override
        public Object getObject(int columnIndex) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return values[columnIndex];
        }

        @Override
        public <T> T getObject(int columnIndex, Class<T> clasz) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return clasz.cast(values[columnIndex]);
        }

        @Override
        public String getString(int columnIndex) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return (String) values[columnIndex];
        }

        @Override
        public Short getShort(int columnIndex) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return (Short) values[columnIndex];
        }

        @Override
        public Integer getInteger(int columnIndex) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return (Integer) values[columnIndex];
        }

        @Override
        public Long getLong(int columnIndex) {
            --columnIndex;
            if (isNull(columnIndex)) {
                return null;
            }
            return (Long) values[columnIndex];
        }

        @Override
        public int getColumnSize() {
            return values.length;
        }

        @Override
        public Class<?> getType(int columnIndex) {
            --columnIndex;
            return types[columnIndex];
        }

        @Override
        public List<String> getColumns() {
            if (columns == null) {

                try {
                    /*
                     * 请求元数据
                     */
                    TGetResultSetMetadataReq metadataReq = new TGetResultSetMetadataReq(operation);
                    TGetResultSetMetadataResp metadataResp = client.GetResultSetMetadata(metadataReq);

                    checkStatus(metadataResp.getStatus(), null);

                    TTableSchema tableSchema = metadataResp.getSchema();

                    if (tableSchema != null) {
                        List<TColumnDesc> columnDescs = tableSchema.getColumns();
                        columns = Lists.newArrayListWithExpectedSize(columnDescs.size());
                        for (TColumnDesc tColumnDesc : columnDescs) {
                            columns.add(tColumnDesc.getColumnName());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (columns == null) {
                    columns = Lists.newArrayListWithExpectedSize(0);
                }
            }
            return columns;
        }

        @Override
        public void close() {
            this.closed = true;
        }
    }

    /**
     * 心跳
     */
    @Override
    public void run() {
        log.info("Impala client heart beats.");
        for (ExecHandler handler : Lists.newArrayList(handlers.values())) {
            try {
                ExecSummary summary = getExecSummary(handler);
                ResultListener resultListener = handler.getResultListener();
                ExecStatus status = summary.getStatus();
                if (status.isActive()) {
                    /*
                     * 正在执行，查询执行信息
                     */
                    if (resultListener != null && summary.getProgress() != null) {
                        resultListener.progress(summary.getProgress());
                    }
                } else if (status.hasError()) {
                    if (resultListener != null) {
                        resultListener.error(summary.getStatus());
                    }
                    release(handler.getQueryId());
                } else {
                    fetchResult(handler);
                }
            } catch (Exception e) {
                if (handler.markError() >= Constant.DEFAULT_ERROR_INDURANCE) {
                    release(handler.getQueryId());
                }
                log.warn("Fetch status error.", e);
            }
        }
    }

    @Override
    public int getExecutionCount() {
        return parallel;
    }
}
