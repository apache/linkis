### Linkis快速使用文档
#### 1.概述
Linkis为用户提供了Java客户端的实现，用户可以使用UJESClient对Linkis后台服务实现快速访问。

#### 2 快速运行

   我们在ujes/client/src/test模块下，提供了UJESClient的两个测试类：
        
        com.webank.wedatasphere.linkis.ujes.client.UJESClientImplTestJ # 基于Java实现的测试类
        com.webank.wedatasphere.linkis.ujes.client.UJESClientImplTest # 基于Scala实现的测试类

   如果您clone了Linkis的源代码，可以直接运行这两个测试类。
   
   **下面具体介绍如何快速实现一次对Linkis的代码提交执行。**

#### 3 快速实现

##### 3.1 maven依赖

```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-client</artifactId>
  <version>0.9.3</version>
</dependency>
```

#### 3.2 参考实现

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
        // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
        DWSClientConfig clientConfig = ((DWSClientConfigBuilder) (DWSClientConfigBuilder.newBuilder()
                .addUJESServerUrl("http://${ip}:${port}")  //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
                .connectionTimeout(30000)   //connectionTimeOut 客户端连接超时时间
                .discoveryEnabled(true).discoveryFrequency(1, TimeUnit.MINUTES)  //是否启用注册发现，如果启用，会自动发现新启动的Gateway
                .loadbalancerEnabled(true)  // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
                .maxConnectionSize(5)   //指定最大连接数，即最大并发数
                .retryEnabled(false).readTimeout(30000)   //执行失败，是否允许重试
                .setAuthenticationStrategy(new StaticAuthenticationStrategy())   //AuthenticationStrategy Linkis认证方式
                .setAuthTokenKey("${username}").setAuthTokenValue("${password}")))  //认证key，一般为用户名;  认证value，一般为用户名对应的密码
                .setDWSVersion("v1").build();  //Linkis后台协议的版本，当前版本为v1
        
        // 2. 通过DWSClientConfig获取一个UJESClient
        UJESClient client = new UJESClientImpl(clientConfig);

        // 3. 开始执行代码
        JobExecuteResult jobExecuteResult = client.execute(JobExecuteAction.builder()
                .setCreator("LinkisClient-Test")  //creator，请求Linkis的客户端的系统名，用于做系统级隔离
                .addExecuteCode("show tables")   //ExecutionCode 请求执行的代码
                .setEngineType(JobExecuteAction.EngineType$.MODULE$.HIVE()) // 希望请求的Linkis的执行引擎类型，如Spark hive等
                .setUser("johnnwang")   //User，请求用户；用于做用户级多租户隔离
                .build());
        System.out.println("execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());
        
        // 4. 获取脚本的执行状态
        JobStatusResult status = client.status(jobExecuteResult);
        while(!status.isCompleted()) {
            // 5. 获取脚本的执行进度
            JobProgressResult progress = client.progress(jobExecuteResult);
            Utils.sleepQuietly(500);
            status = client.status(jobExecuteResult);
        }
        
        // 6. 获取脚本的Job信息
        JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);
        // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
        String resultSet = jobInfo.getResultSetList(client)[0];
        // 8. 通过一个结果集信息，获取具体的结果集
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

  // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
  val clientConfig = DWSClientConfigBuilder.newBuilder()
    .addUJESServerUrl("http://${ip}:${port}")  //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
    .connectionTimeout(30000)  //connectionTimeOut 客户端连接超时时间
    .discoveryEnabled(true).discoveryFrequency(1, TimeUnit.MINUTES)  //是否启用注册发现，如果启用，会自动发现新启动的Gateway
    .loadbalancerEnabled(true)  // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
    .maxConnectionSize(5)   //指定最大连接数，即最大并发数
    .retryEnabled(false).readTimeout(30000)   //执行失败，是否允许重试
    .setAuthenticationStrategy(new StaticAuthenticationStrategy())  //AuthenticationStrategy Linkis认证方式
    .setAuthTokenKey("${username}").setAuthTokenValue("${password}")  //认证key，一般为用户名;  认证value，一般为用户名对应的密码
    .setDWSVersion("v1").build()  //Linkis后台协议的版本，当前版本为v1
  
  // 2. 通过DWSClientConfig获取一个UJESClient
  val client = UJESClient(clientConfig)

  // 3. 开始执行代码
  val jobExecuteResult = client.execute(JobExecuteAction.builder()
    .setCreator("LinkisClient-Test")  //creator，请求Linkis的客户端的系统名，用于做系统级隔离
    .addExecuteCode("show tables")   //ExecutionCode 请求执行的代码
    .setEngineType(EngineType.SPARK) // 希望请求的Linkis的执行引擎类型，如Spark hive等
    .setUser("${username}").build())  //User，请求用户；用于做用户级多租户隔离
  println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)
  
  // 4. 获取脚本的执行状态
  var status = client.status(jobExecuteResult)
  while(!status.isCompleted) {
  // 5. 获取脚本的执行进度
    val progress = client.progress(jobExecuteResult)
    val progressInfo = if(progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
    println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
    Utils.sleepQuietly(500)
    status = client.status(jobExecuteResult)
  }
  
  // 6. 获取脚本的Job信息
  val jobInfo = client.getJobInfo(jobExecuteResult)
  // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
  val resultSet = jobInfo.getResultSetList(client).head
  // 8. 通过一个结果集信息，获取具体的结果集
  val fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
  println("fileContents: " + fileContents)
  IOUtils.closeQuietly(client)
}
```
