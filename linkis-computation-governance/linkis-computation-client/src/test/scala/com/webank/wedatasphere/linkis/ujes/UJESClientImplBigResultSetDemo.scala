/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.ujes

import java.util.concurrent.TimeUnit

import com.google.gson.Gson
import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.TokenAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.{DWSClientConfig, DWSClientConfigBuilder}
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction.EngineType
import com.webank.wedatasphere.linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
import com.webank.wedatasphere.linkis.ujes.client.response.JobExecuteResult
import org.apache.commons.io.IOUtils


object UJESClientImplBigResultSetDemo {


  def main(args: Array[String]): Unit = {

//    var executeCode = "--set ide.engine.no.limit.allow=true; \n use ai_caixin_ads_ods_mask; \n select * from caixin_click_from_mysql limit 60000;"
//    var executeCode = "--set ide.engine.no.limit.allow=true; \n use hduser02db; \n select zjyilush from t_history_kaps_zjyils limit 1001;"
    var executeCode = "show tables"
//    var user = "jianfuzhang"
    var user = "hadoop"

    if (args.length > 2) {
      helpPrint(user, executeCode)
      return Unit
    } else if (args.length > 1) {
      user = args(0)
      executeCode = args(1)
    } else if (args.length > 0) {
      user = args(0)
    }

    val startTime = System.currentTimeMillis()
    val clientConfig = buildConfig()

    // 2. 通过DWSClientConfig获取一个UJESClient
    val client = UJESClient(clientConfig)

    val jobExecuteResult = executeJob(client, user, executeCode)
    if (null == jobExecuteResult) {
      IOUtils.closeQuietly(client)
    }
    getResult(client, jobExecuteResult, startTime)

  }

  def helpPrint(user : String, code : String, fullPrint : Boolean = false) = {
    println("Usage: java demo.jar  user  [sql code] , only accept less than two params.")
    if (fullPrint) println("user is : " + user  + " , code : [" + code + "]")
  }

  val COMMON_AUTH_KEY : String = "Validation-Code"

  private def buildConfig() : DWSClientConfig = {
    // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
    val clientConfig = DWSClientConfigBuilder.newBuilder()
      .addServerUrl("http://127.0.0.1:9001") //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
      //      .addUJESServerUrl("http://127.0.0.1:9001") //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
      .connectionTimeout(30000) //connectionTimeOut 客户端连接超时时间
      .discoveryEnabled(false).discoveryFrequency(1, TimeUnit.MINUTES) //是否启用注册发现，如果启用，会自动发现新启动的Gateway
      .loadbalancerEnabled(true) // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
      .maxConnectionSize(5) //指定最大连接数，即最大并发数
      .retryEnabled(false).readTimeout(30000) //执行失败，是否允许重试
      .setAuthenticationStrategy(new TokenAuthenticationStrategy()) //AuthenticationStrategy Linkis认证方式
//      .setAuthTokenKey("jianfuzhang").setAuthTokenValue("fcg123456") //认证key，一般为用户名;  认证value，一般为用户名对应的密码
      .setAuthTokenKey(COMMON_AUTH_KEY).setAuthTokenValue("BML-AUTH") //认证key，一般为用户名;  认证value，一般为用户名对应的密码
      .setDWSVersion("v1").build() //Linkis后台协议的版本，当前版本为v1
    clientConfig
  }


  // 3. 开始执行代码
  def executeJob(client: UJESClient, user : String, code : String) : JobExecuteResult = {
    println("user : " + user + ", code : [" + code + "]")
    val startupMap = new java.util.HashMap[String, Any]()
//    startupMap.put("wds.linkis.rm.yarnqueue", "default")
    startupMap.put("wds.linkis.yarnqueue", "dws")
    try {
      val jobExecuteResult = client.execute(JobExecuteAction.builder()
        .setCreator("LinkisClient-Test")  //creator，请求Linkis的客户端的系统名，用于做系统级隔离
        .addExecuteCode(code)   //ExecutionCode 请求执行的代码
        .setEngineType(EngineType.SPARK) // 希望请求的Linkis的执行引擎类型，如Spark hive等
//        .setUser("hduser2203").build())  //User，请求用户；用于做用户级多租户隔离
          .setStartupParams(startupMap)
        .setUser(user).build())  //User，请求用户；用于做用户级多租户隔离
      println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)
      return jobExecuteResult
    } catch {
      case e : Exception => {
        e.printStackTrace()
      }
    }
    null.asInstanceOf[JobExecuteResult]
  }


  private def getResult(client: UJESClient, jobExecuteResult: JobExecuteResult, startTime: Long): Unit = {

    var executeTime = 0l
    try {
      // 4. 获取脚本的执行状态
      var jobInfoResult = client.getJobInfo(jobExecuteResult)
      val sleepTimeMills : Int = 1000
      var sleepTime = 0.0
      while (!jobInfoResult.isCompleted) {
        // 5. 获取脚本的执行进度    ***这里是新接口***
        val progress = client.progress(jobExecuteResult)
        val progressInfo = if (progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
        Utils.sleepQuietly(sleepTimeMills)
        sleepTime = sleepTime + sleepTimeMills / 1000.0
        println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo + ", time : " + sleepTime + "s")
        jobInfoResult = client.getJobInfo(jobExecuteResult)
      }
      if (!jobInfoResult.isSucceed) {
        println("Failed to execute job: " + new Gson().toJson(jobInfoResult))
        IOUtils.closeQuietly(client)
        return
//        throw new Exception(jobInfoResult.getMessage)
      }

      executeTime = System.currentTimeMillis()
      // 6. 获取脚本的Job信息
      val jobInfo = client.getJobInfo(jobExecuteResult)
      // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
      val resultSetList = jobInfoResult.getResultSetList(client)
      println("All result set list:")
      resultSetList.foreach(println)
      val oneResultSet = jobInfo.getResultSetList(client).head
      // 8. 通过一个结果集信息，获取具体的结果集
//      val pageSize = 5000
      val pageSize = 300
      var page = 1
      var onePageResultSet = client.resultSet(ResultSetAction.builder()
        .setPath(oneResultSet).setUser(jobExecuteResult.getUser)
        .setPage(page).setPageSize(pageSize)
        .build())

      //// fetch context
      println(onePageResultSet.getFileContent)
      println("totalLine : " + onePageResultSet.totalLine)
      var lineCount = 0
      while (onePageResultSet.totalLine > 0 && onePageResultSet.totalLine <= pageSize) {
        page = page + 1
        lineCount = lineCount + onePageResultSet.totalLine
        onePageResultSet = client.resultSet(ResultSetAction.builder()
          .setPath(oneResultSet).setUser(jobExecuteResult.getUser)
          .setPage(page).setPageSize(pageSize)
          .build())
        println("line num : " + onePageResultSet.totalLine)
        println("total page : " + onePageResultSet.totalPage)
        /// fetch num
        println(onePageResultSet.getFileContent)
      }
      println(new Gson().toJson(onePageResultSet))
//      val fileContents = resultSet.getFileContent
//      println("First fileContents: ")
//      println(fileContents)
      println("total lines : " + lineCount)
    } catch {
      case e: Exception => {
        e.printStackTrace()
        IOUtils.closeQuietly(client)
      }
    }
    IOUtils.closeQuietly(client)
    val getresultTime = System.currentTimeMillis()
    println("executeTime : " + (executeTime - startTime) / 1000.0 + "s")
    println("getresultTime : " + (getresultTime - executeTime) / 1000.0 + "s")
  }


}

