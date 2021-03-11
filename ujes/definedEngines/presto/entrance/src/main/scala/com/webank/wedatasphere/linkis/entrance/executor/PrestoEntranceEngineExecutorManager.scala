/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.webank.wedatasphere.linkis.entrance.executor

import java.net.URI
import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{AtomicLong, AtomicReference}
import java.util.{Collections, Locale, Optional, TimeZone}

import com.facebook.presto.client.ClientSession
import com.facebook.presto.spi.resourceGroups.SelectionCriteria
import com.facebook.presto.spi.security.SelectedRole
import com.facebook.presto.spi.session.ResourceEstimates
import com.webank.wedatasphere.linkis.common.conf.ByteType
import com.webank.wedatasphere.linkis.common.log.LogUtils
import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.execute.impl.EntranceExecutorManagerImpl
import com.webank.wedatasphere.linkis.entrance.utils.PrestoResourceUtils
import com.webank.wedatasphere.linkis.protocol.config.{RequestQueryAppConfigWithGlobal, ResponseQueryConfig}
import com.webank.wedatasphere.linkis.protocol.utils.TaskUtils
import com.webank.wedatasphere.linkis.resourcemanager._
import com.webank.wedatasphere.linkis.resourcemanager.client.ResourceManagerClient
import com.webank.wedatasphere.linkis.resourcemanager.exception.RMWarnException
import com.webank.wedatasphere.linkis.rpc.Sender
import com.webank.wedatasphere.linkis.scheduler.executer.{Executor, ExecutorState}
import com.webank.wedatasphere.linkis.scheduler.listener.ExecutorListener
import com.webank.wedatasphere.linkis.scheduler.queue.{GroupFactory, SchedulerEvent}
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConverters._
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
  @Autowired
  private var rmClient: ResourceManagerClient = _

  private val idGenerator = new AtomicLong(0)

  override def setExecutorListener(executorListener: ExecutorListener): Unit = super.setExecutorListener(executorListener)

  override def askExecutor(schedulerEvent: SchedulerEvent): Option[Executor] = schedulerEvent match {
    case job: EntranceJob =>
      val executor = createExecutor(job)
      initialEntranceEngine(executor)
      Some(executor)
    case _ => None
  }

  override def askExecutor(schedulerEvent: SchedulerEvent, wait: Duration): Option[Executor] = {
    askExecutor(schedulerEvent)
  }

  override protected def createExecutor(schedulerEvent: SchedulerEvent): EntranceEngine = schedulerEvent match {
    case job: EntranceJob =>
      //FIXME replace datasource config
      val configMap = getUserConfig(job)

      val clientSession = getClientSession(job.getUser, configMap)

      val criteria = new SelectionCriteria(true, job.getUser, Optional.of(clientSession.getSource), Collections.emptySet(), new ResourceEstimates(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()), Optional.empty())
      val memory: Long = new ByteType(PRESTO_REQUEST_MEMORY.getValue(configMap)).toLong
      val groupName = PrestoResourceUtils.getGroupName(criteria, PRESTO_RESOURCE_CONFIG_PATH.getValue(configMap))
      val requestResource = new InstanceAndPrestoResource(new InstanceResource(1), new PrestoResource(memory, 1, groupName, PRESTO_URL.getValue))
      rmClient.requestResource(job.getUser, job.getCreator, requestResource) match {
        case NotEnoughResource(reason) => throw new RMWarnException(40001, LogUtils.generateWarn(reason))
        case AvailableResource(ticketId) =>
          rmClient.resourceInited(UserResultResource(ticketId, job.getUser), requestResource)
          new PrestoEntranceEngineExecutor(idGenerator.getAndIncrement(), job, new AtomicReference[ClientSession](clientSession), okHttpClient, () => rmClient.resourceReleased(UserResultResource(ticketId, job.getUser)))
      }
  }

  private def getUserConfig(job: EntranceJob): util.Map[String, String] = {
    val configMap = new util.HashMap[String, String]()
    val runtimeMap: util.Map[String, Any] = TaskUtils.getRuntimeMap(job.getParams)
    val startupMap: util.Map[String, Any] = TaskUtils.getStartupMap(job.getParams)
    startupMap.foreach(item => if (item._2 != null) configMap.put(item._1, item._2.toString))
    runtimeMap.foreach(item => if (item._2 != null) configMap.put(item._1, item._2.toString))

    val sender = Sender.getSender(EntranceConfiguration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)
    val response = sender.ask(RequestQueryAppConfigWithGlobal(job.getUser, job.getCreator, "presto", isMerge = true)).asInstanceOf[ResponseQueryConfig]
    val appConfig = response.getKeyAndValue
    if (appConfig != null) {
      appConfig.foreach(item => if (!configMap.containsKey(item._1)) configMap.put(item._1, item._2))
    }
    configMap
  }

  private def getClientSession(user: String, configMap: util.Map[String, String]): ClientSession = {
    val httpUri: URI = URI.create(PRESTO_URL.getValue(configMap))
    val source: String = PRESTO_SOURCE.getValue(configMap)
    val catalog: String = PRESTO_CATALOG.getValue(configMap)
    val schema: String = PRESTO_SCHEMA.getValue(configMap)

    val properties: util.Map[String, String] = configMap.asScala
      .filter(tuple => tuple._1.startsWith("presto.session."))
      .map(tuple => (tuple._1.substring("presto.session.".length), tuple._2))
      .asJava

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

  override def shutdown(): Unit = {
    super.shutdown()
    engineManager.listEngines(engine => ExecutorState.isAvailable(engine.state))
      .foreach(engine => engine.asInstanceOf[PrestoEntranceEngineExecutor].shutdown())
  }

}
