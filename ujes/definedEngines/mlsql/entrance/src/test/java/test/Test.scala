package test

import java.net.{InetAddress, URI}
import java.util
import java.util.concurrent.TimeUnit

import com.webank.wedatasphere.linkis.common.utils.Utils
import com.webank.wedatasphere.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy
import com.webank.wedatasphere.linkis.httpclient.dws.config.DWSClientConfigBuilder
import com.webank.wedatasphere.linkis.storage.domain._
import com.webank.wedatasphere.linkis.ujes.client.UJESClient
import com.webank.wedatasphere.linkis.ujes.client.request.JobExecuteAction.{EngineType, RunType}
import com.webank.wedatasphere.linkis.ujes.client.request.{JobExecuteAction, ResultSetAction}
import net.sf.json.{JSONArray, JSONObject}
import org.apache.commons.io.IOUtils

object UJESClientImplTest extends App {

  // 1. 配置DWSClientBuilder，通过DWSClientBuilder获取一个DWSClientConfig
  val clientConfig = DWSClientConfigBuilder.newBuilder()
    .addUJESServerUrl("http://allwefantasy:8001") //指定ServerUrl，Linkis服务器端网关的地址,如http://{ip}:{port}
    .connectionTimeout(30000) //connectionTimeOut 客户端连接超时时间
    .discoveryEnabled(true).discoveryFrequency(1, TimeUnit.MINUTES) //是否启用注册发现，如果启用，会自动发现新启动的Gateway
    .loadbalancerEnabled(true) // 是否启用负载均衡，如果不启用注册发现，则负载均衡没有意义
    .maxConnectionSize(5) //指定最大连接数，即最大并发数
    .retryEnabled(false).readTimeout(30000) //执行失败，是否允许重试
    .setAuthenticationStrategy(new StaticAuthenticationStrategy()) //AuthenticationStrategy Linkis认证方式
    .setAuthTokenKey("allwefantasy").setAuthTokenValue("allwefantasy") //认证key，一般为用户名;  认证value，一般为用户名对应的密码
    .setDWSVersion("v1").build() //Linkis后台协议的版本，当前版本为v1

  // 2. 通过DWSClientConfig获取一个UJESClient
  val client = UJESClient(clientConfig)

  // 3. 开始执行代码
  val params = new util.HashMap[String, Any]()
  params.put("mlsql.url", "http://127.0.0.1:9003")
  val jobExecuteResult = client.execute(JobExecuteAction.builder()
    .setCreator("allwefantasy") //creator，请求Linkis的客户端的系统名，用于做系统级隔离
    .addExecuteCode("!show jobs;") //ExecutionCode 请求执行的代码
    .setEngineType(new EngineType {
    override val toString: String = "mlsql"

    val ML = new RunType {
      override val toString: String = "mlsql"
    }

    override def getDefaultRunType: RunType = ML
  }) // 希望请求的Linkis的执行引擎类型，如Spark hive等
    .setUser("allwefantasy").setParams(params).build()) //User，请求用户；用于做用户级多租户隔离
  println("execId: " + jobExecuteResult.getExecID + ", taskId: " + jobExecuteResult.taskID)

  // 4. 获取脚本的执行状态
  var status = client.status(jobExecuteResult)
  while (!status.isCompleted) {
    // 5. 获取脚本的执行进度
    val progress = client.progress(jobExecuteResult)
    val progressInfo = if (progress.getProgressInfo != null) progress.getProgressInfo.toList else List.empty
    println("progress: " + progress.getProgress + ", progressInfo: " + progressInfo)
    Utils.sleepQuietly(500)
    status = client.status(jobExecuteResult)
  }
  //  Thread.sleep(30000)
  // 6. 获取脚本的Job信息
  val jobInfo = client.getJobInfo(jobExecuteResult)
  // 7. 获取结果集列表（如果用户一次提交多个SQL，会产生多个结果集）
  val resultSet = jobInfo.getResultSetList(client).head
  // 8. 通过一个结果集信息，获取具体的结果集
  val fileContents = client.resultSet(ResultSetAction.builder().setPath(resultSet).setUser(jobExecuteResult.getUser).build()).getFileContent
  println("fileContents: " + fileContents)

  IOUtils.closeQuietly(client)
}

object Test2 {
  def main(args: Array[String]): Unit = {
    val uri = new URI("lb://merge-gw-13mlsqlentrance192.168.216.157---8889")
    println(uri.getHost)

    val uri2 = new URI("lb://mlsqlentrance")
    println(uri2.getHost)

    println(Utils.tryCatch(InetAddress.getLocalHost.getCanonicalHostName)(t => sys.env("COMPUTERNAME")))


    def inferDataType(a: Any) = {
      a.getClass match {
        case c: Class[_] if c == classOf[java.lang.String] => (StringType, true)
        case c: Class[_] if c == classOf[java.lang.Short] => (ShortIntType, true)
        case c: Class[_] if c == classOf[java.lang.Integer] => (IntType, true)
        case c: Class[_] if c == classOf[java.lang.Long] => (LongType, true)
        case c: Class[_] if c == classOf[java.lang.Double] => (DoubleType, true)
        case c: Class[_] if c == classOf[java.lang.Byte] => (BinaryType, true)
        case c: Class[_] if c == classOf[java.lang.Float] => (FloatType, true)
        case c: Class[_] if c == classOf[java.lang.Boolean] => (BooleanType, true)
        case c: Class[_] if c == classOf[java.sql.Date] => (DateType, true)
        case c: Class[_] if c == classOf[java.sql.Timestamp] => (TimestampType, true)
        case c: Class[_] if c == classOf[java.math.BigDecimal] => (DecimalType, true)
        case c: Class[_] if c == classOf[java.math.BigInteger] => (DecimalType, true)
        case _ if a.isInstanceOf[JSONArray] => (ListType, true)
        case _ if a.isInstanceOf[JSONObject] => (MapType, true)
      }
    }

    import scala.collection.JavaConverters._
    println(JSONObject.fromObject(
      """
        |{"a":"b","m":1}
      """.stripMargin).asScala.toMap.map(f => inferDataType(f._2)).toList)

  }
}


