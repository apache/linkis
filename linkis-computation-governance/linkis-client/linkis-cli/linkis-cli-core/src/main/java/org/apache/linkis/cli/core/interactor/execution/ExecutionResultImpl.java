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
 
package org.apache.linkis.cli.core.interactor.execution;

import org.apache.linkis.cli.common.entity.execution.ExecutionResult;
import org.apache.linkis.cli.common.entity.execution.jobexec.ExecutionStatus;
import org.apache.linkis.cli.common.entity.execution.jobexec.JobExec;


public class ExecutionResultImpl implements ExecutionResult {
    JobExec execData;
    ExecutionStatus executionStatus;
    Exception exception;

    public ExecutionResultImpl() {
        executionStatus = ExecutionStatus.UNDEFINED;
    }

    public ExecutionResultImpl(JobExec execData, ExecutionStatus executionStatus) {
        this.execData = execData;
        this.executionStatus = executionStatus;
    }

    public ExecutionResultImpl(JobExec execData, ExecutionStatus executionStatus, Exception exception) {
        this.execData = execData;
        this.executionStatus = executionStatus;
        this.exception = exception;
    }

    @Override
    public Object getData() {
        return this.execData;
    }

    @Override
    public ExecutionStatus getExecutionStatus() {
        return this.executionStatus;
    }

    @Override
    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    @Override
    public Exception getException() {
        //TODO
        return null;
    }

    @Override
    public void setException(Exception exception) {
        this.exception = exception;
    }
}
