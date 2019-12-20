### Linkis Java SDK Doc
#### 1.Introduction

Linkis provides a Java client implementation for users to have quick-access to Linkis background services using UJESClient.

#### 2 Quick start

   We provided two test classes under dir ujes/client/src/test：
        
        com.webank.wedatasphere.linkis.ujes.client.UJESClientImplTestJ # Test class based on Java
        com.webank.wedatasphere.linkis.ujes.client.UJESClientImplTest # Test class based on Scala

   If you have cloned the source code of Linkis, you can directly run these two test classes.


   **Below sections introduce about how to write the code to complete a single execution on Linkis**

#### 3 Fast implementation

##### 3.1 maven dependency

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-client</artifactId>
  <version>0.9.2</version>
</dependency>
```

#### 3.2 Sample implementation

- **JAVA**
```java
package com.webank.bdp.dataworkcloud.ujes.client;

import com.webank.wedatasphere.Linkis.common.utils.Utils;
import com.webank.wedatasphere.Linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import com.webank.wedatasphere.Linkis.httpclient.dws.config.DWSClientConfig;
import com.webank.wedatasphere.Linkis.httpclient.dws.config.DWSClientConfigBuilder;
import com.webank.wedatasphere.Linkis.ujes.client.UJESClient;
import com.webank.wedatasphere.Linkis.ujes.client.UJESClientImpl;
import com.webank.wedatasphere.Linkis.ujes.client.request.JobExecuteAction;
import com.webank.wedatasphere.Linkis.ujes.client.request.ResultSetAction;
import com.webank.wedatasphere.Linkis.ujes.client.response.JobExecuteResult;
import com.webank.wedatasphere.Linkis.ujes.client.response.JobInfoResult;
import com.webank.wedatasphere.Linkis.ujes.client.response.JobProgressResult;
import com.webank.wedatasphere.Linkis.ujes.client.response.JobStatusResult;
import org.apache.commons.io.IOUtils;

import java.util.concurrent.TimeUnit;


public class UJESClientImplTestJ{
    public static void main(String[] args){
        // 1. To do the configuration, an instance of DWSClientConfig should be obtained from DWSClientBuilder.
        DWSClientConfig clientConfig = ((DWSClientConfigBuilder) (DWSClientConfigBuilder.newBuilder()
                .addUJESServerUrl("http://${ip}:${port}")  //Specify the ServerUrl，The address of Willink gateway, i.e. http://{ip}:{port}
                .connectionTimeout(30000)   //connectionTimeOut: The connection timeout of the client
                .discoveryEnabled(true).discoveryFrequency(1, TimeUnit.MINUTES)  //Enable service discovery. Once enabled, newly started Gateway will be auto-dicovered.
                .loadbalancerEnabled(true)  // Enable load balancing. Cannot be enabled alone without service discovery enabled.
                .maxConnectionSize(5)   //Max connection size, aka the max concurrent threshold
                .retryEnabled(false).readTimeout(30000)   //whether to retry after failure
                .setAuthenticationStrategy(new StaticAuthenticationStrategy())   //AuthenticationStrategy, The authentication strategy of Linkis
                .setAuthTokenKey("${username}").setAuthTokenValue("${password}")))  //The authentication key，usually the username;The authentication value，usually the password
                .setDWSVersion("v1").build();  //The version of Linkis background protocol, currently v1
        
        // 2. Create a UJESClient from DWSClientConfig
        UJESClient client = new UJESClientImpl(clientConfig);

        // 3. Begin to execute the code
        JobExecuteResult jobExecuteResult = client.execute(JobExecuteAction.builder()
                .setCreator("LinkisClient-Test")  //creator. The name of the system which holds the UJES client, used for system level isolation.
                .addExecuteCode("show tables")   //ExecutionCode. The code which is requested to be executed 
                .setEngineType(JobExecuteAction.EngineType$.MODULE$.HIVE()) // The engine type expected by the client, i.e. Spark, Hive, etc...
                .setUser("johnnwang")   //User, The user who makes this request；Used for user level multi-tenant isolation
                .build());
        System.out.println("execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());
        
        // 4. Synch the status of script execution
        JobStatusResult status = client.status(jobExecuteResult);
        while(!status.isCompleted()) {
            // 5. Synch the status of script execution
            JobProgressResult progress = client.progress(jobExecuteResult);
            Utils.sleepQuietly(500);
            status = client.status(jobExecuteResult);
        }
        
        // 6. Synch the job information of script execution
        JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
        // 7. Fetch the list of result sets(Multiple result sets will be generated if a user submitted multiple SQL at once)
        String resultSet = jobInfo.getResultSetList(client)[0];
        // 8. Fetch detailed result set content with a particular result set info
        Object fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser()).build()).getFileContent();
        System.out.println("fileContents: " + fileContents);
        IOUtils.closeQuietly(client);
    }
}
```

- **SCALA**
```scala

import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.Linkis.common.utils.Utils
import com.webank.wedatasphere.Linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.Linkis.httpclient.dws.config.DWSClientConfigBuilder
import com.webank.wedatasphere.Linkis.ujes.client.request.JobExecuteAction.EngineType
import com.webank.wedatasphere.Linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
import org.apache.commons.io.IOUtils

object UJESClientImplTest extends App {

  // 1. To do the configuration, an instance of DWSClientConfig should be obtained from DWSClientBuilder.
  val clientConfig = DWSClientConfigBuilder.newBuilder()
    .addUJESServerUrl("http://${ip}:${port}")  //Specify the ServerUrl，The address of Willink gateway, i.e. http://{ip}:{port}
    .connectionTimeout(30000)  //connectionTimeOut: The connection timeout of the client
    .discoveryEnabled(true).discoveryFrequency(1, TimeUnit.MINUTES)   //Enable service discovery. Once enabled, newly started Gateway will be auto-dicovered.
    .loadbalancerEnabled(true)  // Enable load balancing. Cannot be enabled alone without service discovery enabled.
    .maxConnectionSize(5)   //Max connection size, aka the max concurrent threshold
    .retryEnabled(false).readTimeout(30000)   //whether to retry after failure
    .setAuthenticationStrategy(new StaticAuthenticationStrategy())   //AuthenticationStrategy, The authentication strategy of Linkis
    .setAuthTokenKey("${username}").setAuthTokenValue("${password}")  //The authentication key，usually the username;The authentication value，usually the password
    .setDWSVersion("v1").build()  //The version of Linkis background protocol, currently v1
  
  // 2. Create a UJESClient from DWSClientConfig
  val client = UJESClient(clientConfig)

  // 3. Begin to execute the code
  val jobExecuteResult = client.execute(JobExecuteAction.builder()
    .setCreator("LinkisClient-Test")  //creator. The name of the system which holds the UJES client, used for system level isolation.
    .addExecuteCode("show tables")   //ExecutionCode. The code which is requested to be executed 
    .setEngineType(EngineType.SPARK) // The engine type expected by the client, i.e. Spark, Hive, etc...
    .setUser("${username}").build())  //User, The user who makes this request；Used for user level multi-tenant isolation
  println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)
  
  // 4. Synch the status of script execution
  var status = client.status(jobExecuteResult)
  while(!status.isCompleted) {
  // 5. Synch the status of script execution
    val progress = client.progress(jobExecuteResult)
    val progressInfo = if(progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
    println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
    Utils.sleepQuietly(500)
    status = client.status(jobExecuteResult)
  }
  
   // 6. Synch the job information of script execution
  val jobInfo = client.getJobInfo(jobExecuteResult)
  // 7. Fetch the list of result sets(Multiple result sets will be generated if a user submitted multiple SQL at once)
  val resultSet = jobInfo.getResultSetList(client).head
  // 8. Fetch detailed result set content with a particular result set info
  val fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
  println("fileContents: " + fileContents)
  IOUtils.closeQuietly(client)
}
```
