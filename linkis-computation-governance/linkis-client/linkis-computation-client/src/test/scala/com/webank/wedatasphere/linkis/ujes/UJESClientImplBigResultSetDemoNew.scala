package com.webank.wedatasphere.linkis.ujes

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.{StaticAuthenticationStrategy, TokenAuthenticationStrategy}
import com.webank.wedatasphere.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import com.webank.wedatasphere.linkis.manager.label.constant.LabelKeyConstant
import com.webank.wedatasphere.linkis.protocol.constants.TaskConstant
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction.EngineType
import com.webank.wedatasphere.linkis.ujes.client.request.{JobExecuteAction, JobSubmitAction, ResultSetAction}
import com.webank.wedatasphere.linkis.ujes.client.response.JobExecuteResult
import org.apache.commons.io.IOUtils

import java.util.concurrent.TimeUnit
import java.util


object UJESClientImplBigResultSetDemoNew {


  def main(args: Array[String]): Unit = {
    val executeCode = "show tables"
    val user = "hadoop"

    // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
    val clientConfig = DWSClientConfigBuilder.newBuilder()
      .addServerUrl("http://${ip}:${port}") //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
      .connectionTimeout(30000) //connectionTimeOut 客户端连接超时时间
      .discoveryEnabled(false).discoveryFrequency(1, TimeUnit.MINUTES) //是否启用注册发现，如果启用，会自动发现新启动的Gateway
      .loadbalancerEnabled(true) // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
      .maxConnectionSize(5) //指定最大连接数，即最大并发数
      .retryEnabled(false).readTimeout(30000) //执行失败，是否允许重试
      .setAuthenticationStrategy(new StaticAuthenticationStrategy()) //AuthenticationStrategy Linkis认证方式
      .setAuthTokenKey("${username}").setAuthTokenValue("${password}") //认证key，一般为用户名;  认证value，一般为用户名对应的密码
      .setDWSVersion("v1").build() //Linkis后台协议的版本，当前版本为v1

    // 2. 通过DWSClientConfig获取一个UJESClient
    val client = UJESClient(clientConfig)

    try {
      // 3. 开始执行代码
      println("user : " + user + ", code : [" + executeCode + "]")
      val startupMap = new java.util.HashMap[String, Any]()
      startupMap.put("wds.linkis.yarnqueue", "default") //启动参数配置
      //指定Label
      val labels: util.Map[String, Any] = new util.HashMap[String, Any]
      //添加本次执行所依赖的的标签，如engineLabel
      labels.put(LabelKeyConstant.ENGINE_TYPE_KEY, "hive-1.2.1")
      labels.put(LabelKeyConstant.USER_CREATOR_TYPE_KEY, "hadoop-IDE")
      labels.put(LabelKeyConstant.CODE_TYPE_KEY, "hql")
      //指定source
      val source: util.Map[String, Any] = new util.HashMap[String, Any]
      source.put(TaskConstant.SCRIPTPATH, "LinkisClient-test")
      val jobExecuteResult = client.submit(JobSubmitAction.builder
        .addExecuteCode(executeCode)
        .setStartupParams(startupMap)
        .setUser(user) //Job提交用户
        .addExecuteUser(user) //实际执行用户
        .setLabels(labels)
        .setSource(source)
        .build) //User，请求用户；用于做用户级多租户隔离
      println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)

      // 4. 获取脚本的执行状态
      var jobInfoResult = client.getJobInfo(jobExecuteResult)
      val sleepTimeMills: Int = 1000
      while (!jobInfoResult.isCompleted) {
        // 5. 获取脚本的执行进度
        val progress = client.progress(jobExecuteResult)
        val progressInfo = if (progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
        println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
        Utils.sleepQuietly(sleepTimeMills)
        jobInfoResult = client.getJobInfo(jobExecuteResult)
      }
      if (!jobInfoResult.isSucceed) {
        println("Failed to execute job: " + jobInfoResult.getMessage)
        throw new Exception(jobInfoResult.getMessage)
      }

      // 6. 获取脚本的Job信息
      val jobInfo = client.getJobInfo(jobExecuteResult)
      // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
      val resultSetList = jobInfoResult.getResultSetList(client)
      println("All result set list:")
      resultSetList.foreach(println)
      val oneResultSet = jobInfo.getResultSetList(client).head
      // 8. 通过一个结果集信息，获取具体的结果集
      val fileContents = client.resultSet(ResultSetAction.builder().setPath(oneResultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
      println("First fileContents: ")
      println(fileContents)
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    }
    IOUtils.closeQuietly(client)
  }

}

