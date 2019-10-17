package com.webank.wedatasphere.linkis.entrance.executer

import java.nio.charset.Charset
import java.util.UUID

import com.webank.wedatasphere.linkis.common.io.FsPath
import com.webank.wedatasphere.linkis.entrance.exception.MLSQLParamsIllegalException
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.persistence.EntranceResultSetEngine
import com.webank.wedatasphere.linkis.protocol.engine.{JobProgressInfo, RequestTask}
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer._
import com.webank.wedatasphere.linkis.storage.domain._
import com.webank.wedatasphere.linkis.storage.resultset.table.{TableMetaData, TableRecord}
import com.webank.wedatasphere.linkis.storage.resultset.{ResultSetFactory, ResultSetWriter}
import net.sf.json.{JSONArray, JSONObject}
import org.apache.commons.lang.StringUtils
import org.apache.http.client.fluent.{Form, Request}
import org.slf4j.LoggerFactory
import tech.mlsql.common.utils.path.PathFun
import tech.mlsql.common.utils.serder.json.JSONTool

/**
  * 2019-10-10 WilliamZhu(allwefantasy@gmail.com)
  */
class MLSQLEngineExecutor(outputPrintLimit: Int, job: MLSQLEntranceJob)
  extends EntranceEngine(id = 0) with SingleTaskOperateSupport with SingleTaskInfoSupport {

  val charset = Charset.forName("utf8")
  private val name: String = Sender.getThisServiceInstance.getInstance
  private val persistEngine = new EntranceResultSetEngine()

  private val LOG = LoggerFactory.getLogger(getClass)

  val jobId = UUID.randomUUID().toString

  protected def executeLine(code: String, storePath: String, alias: String): ExecuteResponse = {
    val realCode = code.trim()
    val output = Request.Post(PathFun(mlsqlUrl).add("/run/script").toPath).
      connectTimeout(60 * 1000).socketTimeout(12 * 60 * 60 * 1000).
      bodyForm(Form.form().
        add("sql", realCode).
        add("sessionPerUser", "true").
        add("jobName", jobId).
        add("owner", job.getUser).build(), charset)
      .execute().returnContent().asString(charset)

    LOG.info("sql execute completed")

    val resultSet = ResultSetFactory.getInstance.getResultSetByType(ResultSetFactory.TABLE_TYPE)
    val resultSetPath = resultSet.getResultSetPath(new FsPath(storePath), alias)
    val resultSetWriter = ResultSetWriter.getResultSetWriter(resultSet, 512 * 1024, resultSetPath)

    import scala.collection.JavaConverters._
    val items = JSONArray.fromObject(output)
    if (items.size() == 0) {
      return SuccessExecuteResponse()
    }

    val columns = JSONObject.fromObject(items.get(0)).asScala.toMap.map { c =>
      Column(c._1.toString, inferDataType(c._2)._1, "")
    }.toArray[Column]

    val metaData = new TableMetaData(columns)
    resultSetWriter.addMetaData(metaData)

    items.asScala.foreach { item =>
      val rawRow = item.asInstanceOf[JSONObject]
      val row = columns.map { c =>
        val value = rawRow.get(c.columnName).asInstanceOf[Any]
        if (value.isInstanceOf[JSONObject] || value.isInstanceOf[JSONArray]) {
          value.toString
        } else value
      }.toArray

      resultSetWriter.addRecord(new TableRecord(row))
    }

    job.setResultSize(items.size())
    job.incrementResultSetPersisted()

    val linkisOutput = if (resultSetWriter != null) resultSetWriter.toString else null


    AliasOutputExecuteResponse(alias, linkisOutput)

  }

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
      case _ if a.isInstanceOf[JSONArray] => (StringType, true)
      case _ if a.isInstanceOf[JSONObject] => (StringType, true)
    }
  }

  override def execute(executeRequest: ExecuteRequest): ExecuteResponse = {
    LOG.info("MLSQLEngineExecutor executing: +" + executeRequest.code)
    if (StringUtils.isEmpty(executeRequest.code)) {
      return IncompleteExecuteResponse("execute codes can not be empty)")
    }
    val storePath = executeRequest match {
      case storePathExecuteRequest: StorePathExecuteRequest => storePathExecuteRequest.storePath
      case _ => ""
    }
    val code = executeRequest.code

    val executeRes = try {
      executeLine(code, storePath, 0.toString)
    } catch {
      case e: Exception =>
        ErrorExecuteResponse(s"fail to execute ${code}", e)
    }
    executeRes match {
      case aliasOutputExecuteResponse: AliasOutputExecuteResponse =>
        persistEngine.persistResultSet(executeRequest.asInstanceOf[MLSQLJobExecuteRequest].job, aliasOutputExecuteResponse)
      case SuccessExecuteResponse() =>
        LOG.info(s"sql execute successfully : ${code}")
      case IncompleteExecuteResponse(_) =>
        LOG.error(s"sql execute failed : ${code}")
      case _ =>
        LOG.warn("no matching exception")
    }
    SuccessExecuteResponse()
  }

  override protected def callExecute(request: RequestTask): EngineExecuteAsynReturn = null

  def mlsqlUrl = {
    val params = job.getParams

    val variable = params.get("variable").asInstanceOf[java.util.Map[String, Any]]
    val url = if (variable != null && variable.get("mlsql.url") != null) {
      variable.get("mlsql.url").toString
    } else {
      params.get("mlsql.url").toString
    }

    if (url == null) {
      throw MLSQLParamsIllegalException("mlsql url is null")
    }
    url
  }

  private def executeSQL(sql: String) = {
    Request.Post(PathFun(mlsqlUrl).add("/run/script").toPath).
      connectTimeout(60 * 1000).socketTimeout(12 * 60 * 60 * 1000).
      bodyForm(Form.form().
        add("sql", sql).
        add("sessionPerUser", "true").
        add("jobName", UUID.randomUUID().toString).
        add("owner", job.getUser).build(), charset)
      .execute().returnContent().asString(charset)
  }

  override def kill(): Boolean = {
    executeSQL(s"!kill ${jobId};")
    true
  }

  override def pause(): Boolean = ???

  override def resume(): Boolean = ???

  override def progress(): Float = {
    val res = executeSQL(s"!show jobs/${jobId};")
    JSONTool.parseJson[List[MLSQLJobInfo]](res).headOption match {
      case Some(item) =>
        item.progress.currentJobIndex.toFloat / item.progress.totalJob
      case None => 0f
    }
  }

  override def getProgressInfo: Array[JobProgressInfo] = Array.empty[JobProgressInfo]

  override def log(): String = "MLSQL Engine is running"

  override def close(): Unit = {}

  def getName: String = name
}

case class MLSQLJobInfo(
                         owner: String,
                         jobType: String,
                         jobName: String,
                         jobContent: String,
                         groupId: String,
                         progress: MLSQLJobProgress,
                         startTime: Long,
                         timeout: Long
                       )

case class MLSQLJobProgress(var totalJob: Long = 0, var currentJobIndex: Long = 0, var script: String = "") {
  def increment = currentJobIndex += 1

  def setTotal(total: Long) = {
    totalJob = total
  }
}
