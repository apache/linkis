/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.ujes.client;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.ujes.client.request.JobExecuteAction;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.apache.linkis.ujes.client.response.JobInfoResult;
import org.apache.linkis.ujes.client.response.JobProgressResult;
import org.apache.linkis.ujes.client.response.JobStatusResult;

import org.apache.commons.io.IOUtils;

import java.util.concurrent.TimeUnit;

@Deprecated
public class UJESClientImplTestJ {
  public static void main(String[] args) {
    // Suggest to use LinkisJobClient to submit job to Linkis.
    DWSClientConfig clientConfig =
        ((DWSClientConfigBuilder)
                (DWSClientConfigBuilder.newBuilder()
                    .addServerUrl("http://localhost:port")
                    .connectionTimeout(30000)
                    .discoveryEnabled(true)
                    .discoveryFrequency(1, TimeUnit.MINUTES)
                    .loadbalancerEnabled(true)
                    .maxConnectionSize(5)
                    .retryEnabled(false)
                    .readTimeout(30000)
                    .setAuthenticationStrategy(new StaticAuthenticationStrategy())
                    .setAuthTokenKey("")
                    .setAuthTokenValue("")))
            .setDWSVersion("v1")
            .build();
    UJESClient client = new UJESClientImpl(clientConfig);

    JobExecuteResult jobExecuteResult =
        client.execute(
            JobExecuteAction.builder()
                .setCreator("UJESClient-Test")
                .addExecuteCode("show tables")
                .setEngineType(JobExecuteAction.EngineType$.MODULE$.HIVE())
                .setUser("")
                .build());
    System.out.println(
        "execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());
    JobStatusResult status = client.status(jobExecuteResult);
    while (!status.isCompleted()) {
      JobProgressResult progress = client.progress(jobExecuteResult);
      System.out.println("progress: " + progress.getProgress());
      Utils.sleepQuietly(500);
      status = client.status(jobExecuteResult);
    }
    JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
    String resultSet = jobInfo.getResultSetList(client)[0];
    Object fileContents =
        client
            .resultSet(
                ResultSetAction.builder()
                    .setPath(resultSet)
                    .setUser(jobExecuteResult.getUser())
                    .build())
            .getFileContent();
    System.out.println("fileContents: " + fileContents);
    IOUtils.closeQuietly(client);
  }
}
