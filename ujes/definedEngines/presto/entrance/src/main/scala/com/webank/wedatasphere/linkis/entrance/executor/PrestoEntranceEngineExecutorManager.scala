package com.webank.wedatasphere.linkis.entrance.executor

import java.net.URI
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, Locale, Optional, TimeZone}

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

import scala.collection.JavaConverters._
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
      //TODO check resourcemanager

      //TODO replace datasource config
      val startupConf = job.getParams.get("configuration").asInstanceOf[util.Map[String, Any]]
        .get("startup").asInstanceOf[util.Map[String, String]]
      val runtimeConf = job.getParams.get("configuration").asInstanceOf[util.Map[String, Any]]
        .get("runtime").asInstanceOf[util.Map[String, String]]
      val clientSession = getClientSession(startupConf, runtimeConf)
      new PrestoEntranceEngineExecutor(job, clientSession, okHttpClient)
  }

  def getClientSession(startupConf: util.Map[String, String], runtimeConf: util.Map[String, String]): ClientSession = {
    val httpUri: URI = URI.create(getFinalConfig(startupConf, PRESTO_URL))
    val user: String = getFinalConfig(startupConf, PRESTO_USER_NAME)
    val source: String = getFinalConfig(startupConf, PRESTO_RESOURCE)
    val catalog: String = getFinalConfig(startupConf, PRESTO_CATALOG)
    val schema: String = getFinalConfig(startupConf, PRESTO_SCHEMA)

    val properties: util.Map[String, String] = runtimeConf

    val clientInfo: String = "Linkis"
    val transactionId: String = null
    val traceToken: util.Optional[String] = Optional.empty()
    val clientTags: util.Set[String] = Collections.emptySet()
    val timeZonId = TimeZone.getDefault.getID
    val locale: Locale = Locale.getDefault
    val resourceEstimates: util.Map[String, String] = Collections.emptyMap()
    val preparedStatements: util.Map[String, String] = Collections.emptyMap()
    val roles: java.util.Map[String, SelectedRole] = Collections.emptyMap()
    val extraCredentials: util.Map[String, String] = Collections.emptyMap()
    //0不设限
    val clientRequestTimeout: io.airlift.units.Duration = new io.airlift.units.Duration(0, TimeUnit.MILLISECONDS)

    new ClientSession(httpUri, user, source, traceToken, clientTags, clientInfo, catalog, schema, timeZonId, locale,
      resourceEstimates, properties, preparedStatements, roles, extraCredentials, transactionId, clientRequestTimeout)
  }

  private def getFinalConfig(config: util.Map[String, String], vars: CommonVars[String]): String = {
    val value = config.get(vars.key)
    if (StringUtils.isEmpty(value)) {
      vars.getValue
    } else {
      value
    }
  }

}
