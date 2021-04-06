package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 执行任务句柄
 *
 * @author dingqihuang
 * @version Sep 20, 2019
 */
@ToString
@NoArgsConstructor
public abstract class ExecHandler<T> implements AutoCloseable {
    private String queryId;
    private T handler;
    private ResultListener resultListener;
    private int errors;

    private boolean isQueued;

    public ExecHandler(String queryId, T handler, ResultListener resultListener) {
        this.queryId = queryId;
        this.handler = handler;
        this.resultListener = resultListener;
        this.errors = 0;
        this.isQueued = false;
    }

    public int markError() {
        return ++errors;
    }

    public String getQueryId() {
        return queryId;
    }

    public T getHandler() {
        return handler;
    }

    public ResultListener getResultListener() {
        return resultListener;
    }

    public int getErrors() {
        return errors;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean queued) {
        isQueued = queued;
    }
}
