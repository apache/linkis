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
 
package org.apache.linkis.cli.application.suite;

import org.apache.linkis.cli.common.entity.execution.Execution;
import org.apache.linkis.cli.common.entity.result.ResultHandler;
import org.apache.linkis.cli.core.interactor.execution.executor.ExecutorBuilder;
import org.apache.linkis.cli.core.interactor.job.JobBuilder;


public class ExecutionSuite {
    Execution execution;
    JobBuilder jobBuilder;
    ExecutorBuilder executorBuilder;
    ResultHandler[] resultHandlers;

//    ModelConverter presenterModelConverter;
//    Presenter resultPresenter;

    public ExecutionSuite(Execution execution, JobBuilder jobBuilder, ExecutorBuilder executorBuilder, ResultHandler... resultHandlers) {
        this.execution = execution;
        this.jobBuilder = jobBuilder;
        this.executorBuilder = executorBuilder;
        this.resultHandlers = resultHandlers;
//        this.presenterModelConverter = presenterModelConverter;
//        this.resultPresenter = resultPresenter;
    }

    public Execution getExecution() {
        return execution;
    }

    public void setExecution(Execution execution) {
        this.execution = execution;
    }

    public JobBuilder getJobBuilder() {
        return jobBuilder;
    }

    public void setJobBuilder(JobBuilder jobBuilder) {
        this.jobBuilder = jobBuilder;
    }

    public ExecutorBuilder getExecutorBuilder() {
        return executorBuilder;
    }

    public void setExecutorBuilder(ExecutorBuilder executorBuilder) {
        this.executorBuilder = executorBuilder;
    }

    public ResultHandler[] getResultHandlers() {
        return resultHandlers;
    }

    public void setResultHandlers(ResultHandler[] resultHandlers) {
        this.resultHandlers = resultHandlers;
    }
}
