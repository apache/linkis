package com.webank.wedatasphere.linkis.entrance.executor

import java.net.URI
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Locale, Optional, TimeZone}

import com.facebook.presto.client.ClientSession
import com.facebook.presto.spi.security.SelectedRole
import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl
import com.webank.wedatasphere.linkis.scheduler.executer.Executor
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, SchedulerEvent}
import okhttp3.OkHttpClient
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration


/**
 * Created by yogafire on 2020/4/30
 */
class PrestoEntranceEngineExecutorManager(groupFactory: GroupFactory,
                                          engineBuilder: EngineBuilder,
                                          engineRequester: EngineRequester,
                                          engineSelector: EngineSelector,
                                          engineManager: EngineManager,
                                          entranceExecutorRulers: Array[EntranceExecutorRuler])
  extends EntranceExecutorManagerImpl(groupFactory, engineBuilder, engineRequester,
    engineSelector, engineManager, entranceExecutorRulers) with Logging {
  info("Presto ExecutorManager Registered")

  @Autowired
  private var okHttpClient: OkHttpClient = _

  override def setExecutorListener(executorListener: ExecutorListener): Unit = super.setExecutorListener(executorListener)

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match {
    case job: EntranceJob => Some(createExecutor(job))
    case _ => None
  }

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = {
    askExecutor(schedulerEvent)
  }

  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceEngine = schedulerEvent match {
    case job: EntranceJob =>
      //FIXME 校验resourcemanager

      //FIXME 之后替换为获取datasource
      val runTimeParams = job.getParams.get("configuration").asInstanceOf[util.Map[String, Any]]
        .get("runtime").asInstanceOf[util.Map[String, String]]
      val clientSession = getClientSession(runTimeParams)
      new PrestoEntranceEngineExecutor(job, clientSession, okHttpClient)
  }

  def getClientSession(config: util.Map[String, String]): ClientSession = {
    val httpUri: URI = URI.create(getFinalConfig(config, PRESTO_URL))
    val user: String = PRESTO_USER_NAME.getValue
    val clientInfo: String = getFinalConfig(config, PRESTO_RESOURCE)
    val catalog: String = getFinalConfig(config, PRESTO_CATALOG)
    val schema: String = getFinalConfig(config, PRESTO_SCHEMA)
    val properties: java.util.Map[String, String] = config.filter(entry => StringUtils.isNotEmpty(entry._1) && entry._1.trim.startsWith("wds.linkis.presto"))

    val transactionId: String = null
    val traceToken: java.util.Optional[String] = Optional.empty()
    val source: String = "linkis"
    val clientTags: java.util.Set[String] = Set[String]()
    val timeZonId = TimeZone.getDefault.getID
    val locale: Locale = Locale.getDefault
    val resourceEstimates: java.util.Map[String, String] = new util.HashMap[String, String](1, 1)
    val preparedStatements: java.util.Map[String, String] = new util.HashMap[String, String](1, 1)
    val roles: java.util.Map[String, SelectedRole] = new util.HashMap[String, SelectedRole](1, 1)
    val extraCredentials: java.util.Map[String, String] = new util.HashMap[String, String](1, 1)
    //0不设限
    val clientRequestTimeout: io.airlift.units.Duration = new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS)

    new ClientSession(httpUri, user, source, traceToken, clientTags, clientInfo, catalog, schema, timeZonId, locale,
      resourceEstimates, properties, preparedStatements, roles, extraCredentials, transactionId, clientRequestTimeout)
  }

  private def getFinalConfig(config: util.Map[String, String], key: CommonVars[String]): String = {
    val value = config.get(key)
    if (StringUtils.isEmpty(value)) {
      key.getValue
    } else {
      value
    }
  }

}
