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

package com.webank.wedatasphere.linkis.cs.server.scheduler.linkisImpl;

import com.webank.wedatasphere.linkis.cs.server.scheduler.HttpJob;
import com.webank.wedatasphere.linkis.scheduler.executer.ExecuteRequest;

import java.util.function.Consumer;

/**
 * Created by patinousward on 2020/2/18.
 */
public class CsExecuteRequest implements ExecuteRequest, JobToExecuteRequestConsumer<HttpJob> {

    private HttpJob httpJob;

    // TODO: 2020/3/3 变量名修改
    private Consumer<HttpJob> jobConsumer;

    @Override
    public String code() {
        return null;
    }


    @Override
    public HttpJob get() {
        return this.httpJob;
    }

    @Override
    public void set(HttpJob httpJob) {
        this.httpJob = httpJob;
    }

    @Override
    public Consumer<HttpJob> getConsumer() {
        return this.jobConsumer;
    }

    @Override
    public void setConsuemr(Consumer<HttpJob> jobConsumer) {
        this.jobConsumer = jobConsumer;
    }
}
