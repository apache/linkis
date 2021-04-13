package com.webank.wedatasphere.linkis.engine.impala.client.protocol;
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
import com.webank.wedatasphere.linkis.engine.impala.client.ResultListener;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *
 * Created by dingqihuang on Sep 20, 2019
 *
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
