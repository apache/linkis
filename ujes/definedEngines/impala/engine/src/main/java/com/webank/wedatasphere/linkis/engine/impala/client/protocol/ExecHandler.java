package com.webank.wedatasphere.linkis.engine.impala.client.protocol;

import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;

/**
 * 执行任务句柄
 * 
 * @author dingqihuang
 * @version Sep 20, 2019
 */

public class ExecHandler {
    private String queryId;
    private Object handler;
    private ResultListener resultListener;
    private int errors;

    private boolean isQueued;

    /**
     * @param queryId
     * @param handler
     * @param resultListener
     * @param errors
     * @param isQueued
     */
    public ExecHandler(String queryId, Object handler, ResultListener resultListener, int errors, boolean isQueued) {
        super();
        this.queryId = queryId;
        this.handler = handler;
        this.resultListener = resultListener;
        this.errors = errors;
        this.isQueued = isQueued;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    public ResultListener getResultListener() {
        return resultListener;
    }

    public void setResultListener(ResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public boolean isQueued() {
        return isQueued;
    }

    public void setQueued(boolean isQueued) {
        this.isQueued = isQueued;
    }

    public int markError() {
        return ++errors;
    }
}
