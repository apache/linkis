### Linkis Quick Start

#### 1.Summary
Linkis provides a client implementation for users to have quick-access to Linkis background services using UJESClient.
#### 2.How to Use UJESClient
**2.1 Client Configuration**

Configurations are necessary before instantiating a UJESClient object. To do the configuration, an instance of DWSClientConfig should be obtained from DWSClientBuilder. Parameters should be specified as below:
- ServerUrl: The address of Linkis gateway, i.e. http://{ip}:{port}
- connectionTimeOut: The connection timeout of the client
- AuthenticationStrategy: The authentication strategy of Linkis
- AuthTokenKey: The authentication key，usually the username
- AuthTokenValue: The authentication value，usually the password
- DWSVersion: The version of Linkis background protocol, currently v1

**2.2 UJESClient Instantiation**

The instantiation of UJESClient is very convenient. Simply passing a ClientConfig object configured as mentioned in 2.1 can easily instantiate a new object.

**2.3 Service Request by UJESClient**

After the instantiation of UJESClient, the 'execute' method is invokable to request the background services of Linkis. An instance of JobExectutionAction should be passed as parameter, which can be obtained by invoking JobExecutionAction.builder() with following parameters:
- creator: The name of the system which holds the UJES client and requests for Linkis
- EngineType: The engine type expected by the client, i.e. Spark, Hive, etc...
- User: The user who makes this request
- ExecutionCode: The code which is requested to be executed
After the UJESClient submitted the JobExecutionAction instance to Linkis services, a JobExecutionResult with execId and taskID information would be returned. To track the logs or the status of a task, users can pass the corresponding JobExecuteResult as the parameter to the 'log' or 'status' methods of UJESClient.

#### 3.Sample Implementation

- **JAVA**
```java
package com.webank.bdp.dataworkcloud.ujes.client;

import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.
StaticAuthenticationStrategy;
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfig;
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import com.webank.wedatasphere.linkis.ujes.client.UJESClient;
import com.webank.wedatasphere.linkis.ujes.client.UJESClientImpl;
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction;
import com.webank.wedatasphere.linkis.ujes.client.request.ResultSetAction;
import com.webank.wedatasphere.linkis.ujes.client.response.JobExecuteResult;
import com.webank.wedatasphere.linkis.ujes.client.response.JobInfoResult;
import com.webank.wedatasphere.linkis.ujes.client.response.JobProgressResult;
import com.webank.wedatasphere.linkis.ujes.client.response.JobStatusResult;
import org.apache.commons.io.IOUtils;

import java.util.concurrent.TimeUnit;


public class UJESClientImplTestJ{
    public static void main(String[] args){
        DWSClientConfig clientConfig = ((DWSClientConfigBuilder) (DWSClientConfigBuilder.newBuilder().addUJESServerUrl("http://${ip}:${port}")
                .connectionTimeout(30000).discoveryEnabled(true)
                .discoveryFrequency(1, TimeUnit.MINUTES)
                .loadbalancerEnabled(true).maxConnectionSize(5)
                .retryEnabled(false).readTimeout(30000)
                .setAuthenticationStrategy(new StaticAuthenticationStrategy()).setAuthTokenKey("${username}")
                .setAuthTokenValue("${password}"))).setDWSVersion("v1").build();
        UJESClient client = new UJESClientImpl(clientConfig);

        JobExecuteResult jobExecuteResult = client.execute(JobExecuteAction.builder().setCreator("UJESClient-Test")
                .addExecuteCode("show tables")
                .setEngineType(JobExecuteAction.EngineType$.MODULE$.HIVE()).setUser("${username}").build());
        System.out.println("execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());
        JobStatusResult status = client.status(jobExecuteResult);
        while(!status.isCompleted()) {
            JobProgressResult progress = client.progress(jobExecuteResult);
            Utils.sleepQuietly(500);
            status = client.status(jobExecuteResult);
        }
        JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
        String resultSet = jobInfo.getResultSetList(client)[0];
        Object fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser()).build()).getFileContent();
        System.out.println("fileContents: " + fileContents);
        IOUtils.closeQuietly(client);
    }
}
```

- **SCALA**
```scala

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.
StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.
DWSClientConfigBuilder
import com.webank.wedatasphere.linkis.ujes.client.request.
JobExecuteAction.EngineType
import com.webank.wedatasphere.linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
import org.apache.commons.io.IOUtils

object UJESClientImplTest extends App {

  val clientConfig = DWSClientConfigBuilder.newBuilder().addUJESServerUrl("http://${ip}:${port}")
    .connectionTimeout(30000).discoveryEnabled(true)
    .discoveryFrequency(1, TimeUnit.MINUTES)
    .loadbalancerEnabled(true).maxConnectionSize(5)
    .retryEnabled(false).readTimeout(30000)
    .setAuthenticationStrategy(new StaticAuthenticationStrategy()).setAuthTokenKey("${username}")
    .setAuthTokenValue("${password}").setDWSVersion("v1").build()
  val client = UJESClient(clientConfig)

  val jobExecuteResult = client.execute(JobExecuteAction.builder().setCreator("UJESClient-Test")
    .addExecuteCode("show tables")
    .setEngineType(EngineType.SPARK).setUser("${username}").build())
  println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)
  var status = client.status(jobExecuteResult)
  while(!status.isCompleted) {
    val progress = client.progress(jobExecuteResult)
    val progressInfo = if(progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
    println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
    Utils.sleepQuietly(500)
    status = client.status(jobExecuteResult)
  }
  val jobInfo = client.getJobInfo(jobExecuteResult)
  val resultSet = jobInfo.getResultSetList(client).head
  val fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
  println("fileContents: " + fileContents)
  IOUtils.closeQuietly(client)
}
```
