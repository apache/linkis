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
 
package org.apache.linkis.cli.application.driver;

import org.apache.linkis.cli.application.driver.context.LinkisClientDriverContext;
import org.apache.linkis.cli.application.interactor.job.LinkisJob;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.ujes.client.response.*;

/**
 * @description: Driver should encapsulate all the methods we need to interact with Linkis
 */
public interface LinkisClientDriver {

    void initDriver(LinkisClientDriverContext context) throws LinkisClientRuntimeException;

    void close();

    void checkInit() throws LinkisClientRuntimeException;

    JobSubmitResult submit(LinkisJob job) throws LinkisClientRuntimeException;

    JobInfoResult queryJobInfo(String user, String taskID) throws LinkisClientRuntimeException;

    JobProgressResult queryProgress(String user, String taskID, String execId) throws LinkisClientRuntimeException;

    JobLogResult queryRunTimeLogFromLine(String user, String taskID, String execID, int fromLine) throws LinkisClientRuntimeException;

    OpenLogResult queryPersistedLogAll(String logPath, String user, String taskID) throws LinkisClientRuntimeException;

    String[] queryResultSetPaths(String user, String taskID, String resultLocation);

    ResultSetResult queryResultSetGivenResultSetPath(String resultSetPath, String user, Integer page, Integer pageSize);

    ResultSetResult[] queryAllResults(String user, String taskID, String resultSetLocation) throws LinkisClientRuntimeException;

    JobKillResult kill(String user, String taskId, String execId) throws LinkisClientRuntimeException;

    LinkisClientDriverContext getContext();
}