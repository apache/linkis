### Linkis快速使用文档
#### 1.概述
Linkis为用户提供了客户端的实现，用户可以使用UJESClient对Linkis后台服务实现快速访问。
#### 2.UJESClient使用

**2.1 Maven引入**
```xml
<dependency>
  <groupId>com.webank.wedatasphere.Linkis</groupId>
  <artifactId>Linkis-ujes-client</artifactId>
  <version>0.5.0</version>
</dependency>
```


**2.2 Client配置**

UJESClient的实例化需要事先进行配置。配置的方式是通过DWSClientBuilder进行构建，获取DWSClientConfig的实例，构建需要设定的参数如下
- ServerUrl Linkis服务器端网关的地址,如http://{ip}:{port}
- connectionTimeOut 客户端连接超时时间
- AuthenticationStrategy Linkis认证方式
- AuthTokenKey 认证key，一般为用户名
- AuthTokenValue 认证value，一般为用户名对应的密码
- DWSVersion Linkis后台协议的版本，当前版本为v1

**2.3 UJESClient实例化**

UJESClient的实例化很简单，只需要传入的2.1中已经配置好的ClientConfig实例就可以实例化一个对象。

**2.4 UJESClient请求服务**

UJESClient实例化之后,可以调用execute方法请求Linkis的后台服务，execute需要传入JobExecutionAction的实例作为参数，JobExecutionAction实例可以通过JobExecutionAction.builder()进行构建，需要传入的参数如下
- creator 请求Linkis的UJES客户端所在的系统名
- EngineType 希望请求的Linkis的执行引擎类型，如Spark hive等
- User 请求用户
- ExecutionCode 请求执行的代码
UJESClient将JobExecutionAction的实例提交到Linkis服务端后，会返回一个JobExecuteResult，其中有execID和taskID，
用户想要查看任务的日志或者状态等信息，可以使用UJESClient的log和status方法，传入返回的JobExecuteResult作为参数可以获取到。

#### 3.参考实现

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
        DWSClientConfig clientConfig = ((DWSClientConfigBuilder) (DWSClientConfigBuilder.newBuilder().addUJESServerUrl("http://${ip}:${port}")
                .connectionTimeout(30000).discoveryEnabled(true)
                .discoveryFrequency(1, TimeUnit.MINUTES)
                .loadbalancerEnabled(true).maxConnectionSize(5)
                .retryEnabled(false).readTimeout(30000)
                .setAuthenticationStrategy(new StaticAuthenticationStrategy()).setAuthTokenKey("johnnwang")
                .setAuthTokenValue("Abcd1234"))).setDWSVersion("v1").build();
        UJESClient client = new UJESClientImpl(clientConfig);

        JobExecuteResult jobExecuteResult = client.execute(JobExecuteAction.builder().setCreator("UJESClient-Test")
                .addExecuteCode("show tables")
                .setEngineType(JobExecuteAction.EngineType$.MODULE$.HIVE()).setUser("johnnwang").build());
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

import com.webank.wedatasphere.Linkis.common.utils.Utils
import com.webank.wedatasphere.Linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.Linkis.httpclient.dws.config.DWSClientConfigBuilder
import com.webank.wedatasphere.Linkis.ujes.client.request.JobExecuteAction.EngineType
import com.webank.wedatasphere.Linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
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
