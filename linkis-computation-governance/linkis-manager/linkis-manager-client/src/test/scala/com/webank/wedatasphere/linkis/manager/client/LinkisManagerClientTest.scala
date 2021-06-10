package com.webank.wedatasphere.linkis.manager.client

import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfigBuilder
import com.webank.wedatasphere.linkis.manager.client.request.EngineListAction

import java.util.concurrent.TimeUnit

object LinkisManagerClientTest {
  def main(args: Array[String]): Unit = {
    var user = "hdfs"
    // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
    val clientConfig = DWSClientConfigBuilder.newBuilder()
      .addServerUrl(s"http://{ip}:{port}")  //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
      .connectionTimeout(30000)  //connectionTimeOut 客户端连接超时时间
      .discoveryEnabled(false).discoveryFrequency(1, TimeUnit.MINUTES)  //是否启用注册发现，如果启用，会自动发现新启动的Gateway
      .loadbalancerEnabled(true)  // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
      .maxConnectionSize(5)   //指定最大连接数，即最大并发数
      .retryEnabled(false).readTimeout(30000)   //执行失败，是否允许重试
      .setAuthenticationStrategy(new StaticAuthenticationStrategy())  //AuthenticationStrategy Linkis认证方式
      .setAuthTokenKey("${username}").setAuthTokenValue("${password}")  //认证key，一般为用户名;  认证value，一般为用户名对应的密码
      .setDWSVersion("v1").build()  //Linkis后台协议的版本，当前版本为v1

    val client = LinkisManagerClient(clientConfig)
    val result = client.listUserEngines(EngineListAction.builder().setUser(user).build())
    println(result.getData)
    println(result.getResponseBody)

  }
}
