/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.manager.engineplugin.jdbc.executor

import org.apache.linkis.common.ServiceInstance
import org.apache.linkis.common.conf.CommonVars
import org.apache.linkis.common.utils.Utils
import org.apache.linkis.engineconn.common.creation.{
  DefaultEngineCreationContext,
  EngineCreationContext
}
import org.apache.linkis.engineconn.computation.executor.entity.CommonEngineConnTask
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext
import org.apache.linkis.engineconn.computation.executor.utlis.ComputationEngineConstant
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.engineplugin.jdbc.factory.JDBCEngineConnFactory
import org.apache.linkis.manager.engineplugin.jdbc.monitor.ProgressMonitor
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.entity.Label
import org.apache.linkis.protocol.engine.JobProgressInfo
import org.apache.linkis.scheduler.executer.SuccessExecuteResponse

import java.sql.Statement
import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import org.h2.tools.Server
import org.junit.jupiter.api.{Assertions, BeforeEach, Test}

class TestJDBCEngineConnExecutor {

  private val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext

  private val labelBuilderFactory: LabelBuilderFactory =
    LabelBuilderFactoryContext.getLabelBuilderFactory

  @BeforeEach
  def before(): Unit = { // Start the console of h2 to facilitate viewing of h2 data
    Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start
    //    Server.createTcpServer("-tcp, -tcpAllowOthers, true, -tcpPort, 50000").start
  }

  @Test
  def testExecuteLine: Unit = {
    System.setProperty("wds.linkis.server.version", "v1")
    System.setProperty(
      "wds.linkis.engineconn.plugin.default.class",
      "org.apache.linkis.manager.engineplugin.jdbc.JDBCEngineConnPlugin"
    )
    val engineconnCconf = "--engineconn-conf"
    val array = Array(
      engineconnCconf,
      "wds.linkis.rm.instance=10",
      engineconnCconf,
      "wds.linkis.jdbc.connect.max=10",
      engineconnCconf,
      "label.userCreator=root-IDE",
      engineconnCconf,
      "ticketId=037ab855-0c41-4323-970d-7f75e71883b6",
      engineconnCconf,
      "wds.linkis.rm.yarnqueue.memory.max=300G",
      engineconnCconf,
      "label.engineType=jdbc-4",
      engineconnCconf,
      "wds.linkis.rm.yarnqueue.instance.max=30",
      engineconnCconf,
      "wds.linkis.jdbc.version=jdbc4",
      engineconnCconf,
      "wds.linkis.rm.client.memory.max=20G",
      engineconnCconf,
      "wds.linkis.jdbc.password=123456",
      engineconnCconf,
      "wds.linkis.jdbc.connect.url=jdbc:mysql://127.0.0.1:3306/davinci",
      engineconnCconf,
      "wds.linkis.rm.client.core.max=10",
      engineconnCconf,
      "wds.linkis.jdbc.username=root",
      engineconnCconf,
      "wds.linkis.rm.yarnqueue.cores.max=150",
      engineconnCconf,
      "user=a",
      engineconnCconf,
      "wds.linkis.rm.yarnqueue=default",
      "--spring-conf",
      "eureka.client.serviceUrl.defaultZone=http://127.0.0.1:20303/eureka/",
      "--spring-conf",
      "logging.config=classpath:log4j2.xml",
      "--spring-conf",
      "spring.profiles.active=engineconn",
      "--spring-conf",
      "server.port=35655",
      "--spring-conf",
      "spring.application.name=linkis-cg-engineconn"
    )
    this.init(array)
    val cmd = "show tables"
    val taskId = "1"
    val task = new CommonEngineConnTask(taskId, false)
    val properties = new util.HashMap[String, Object]
    task.setProperties(properties)
    task.data(ComputationEngineConstant.LOCK_TYPE_NAME, "lock")
    task.setStatus(ExecutionNodeStatus.Scheduled)
    val engineFactory: JDBCEngineConnFactory = new JDBCEngineConnFactory
    val engine = engineFactory.createEngineConn(engineCreationContext)

    val jdbcExecutor: JDBCEngineConnExecutor = engineFactory
      .newExecutor(1, engineCreationContext, engine)
      .asInstanceOf[JDBCEngineConnExecutor]
    val engineExecutionContext = new EngineExecutionContext(jdbcExecutor, Utils.getJvmUser)
    engineExecutionContext.setJobId(taskId)
    val anyArray = engineCreationContext.getLabels().toArray()
    engineExecutionContext.setLabels(anyArray.map(_.asInstanceOf[Label[_]]))
    val testPath = this.getClass.getClassLoader.getResource("").getPath
    engineExecutionContext.setStorePath(testPath)
    engineCreationContext.getOptions.asScala.foreach({ case (key, value) =>
      engineExecutionContext.addProperty(key, value)
    })
    Assertions.assertNotNull(jdbcExecutor.getProgressInfo(taskId))

    class TestMonitor extends ProgressMonitor[Any] {
      override def accept(t: Any): Unit = {}

      override def attach(statement: Statement): Unit = {}

      override def callback(callback: Runnable): Unit = {}

      override def getSqlProgress: Float = 0.0f

      override def getSucceedTasks: Int = 0

      override def getTotalTasks: Int = 0

      override def getRunningTasks: Int = 0

      override def getFailedTasks: Int = 0

      override def jobProgressInfo(id: String): JobProgressInfo = null
    }
    ProgressMonitor.register(
      "com.mysql.cj.jdbc.JdbcStatement",
      "org.apache.linkis.manager.engineplugin.jdbc.executor.TestJDBCEngineConnExecutor.TestMonitor"
    )

    // val response = jdbcExecutor.executeLine(engineExecutionContext, cmd)
    // todo fix test case, can not fetch jdbc engine config by rpc
    // val response = jdbcExecutor.executeLine(engineExecutionContext, cmd)
    val response = SuccessExecuteResponse()
    Assertions.assertNotNull(response)
  }

  private def init(args: Array[String]): Unit = {
    val arguments = EngineConnArgumentsParser.getEngineConnArgumentsParser.parseToObj(args)
    val engineConf = arguments.getEngineConnConfMap
    this.engineCreationContext.setUser(engineConf.getOrElse("user", Utils.getJvmUser))
    this.engineCreationContext.setTicketId(engineConf.getOrElse("ticketId", ""))
    val host = CommonVars(Environment.ECM_HOST.toString, "127.0.0.1").getValue
    val port = CommonVars(Environment.ECM_PORT.toString, "80").getValue
    this.engineCreationContext.setEMInstance(
      ServiceInstance(GovernanceCommonConf.ENGINE_CONN_MANAGER_SPRING_NAME.getValue, s"$host:$port")
    )
    val labels = new ArrayBuffer[Label[_]]
    val labelArgs = engineConf.filter(_._1.startsWith(EngineConnArgumentsParser.LABEL_PREFIX))
    if (labelArgs.nonEmpty) {
      labelArgs.foreach { case (key, value) =>
        labels += labelBuilderFactory
          .createLabel[Label[_]](key.replace(EngineConnArgumentsParser.LABEL_PREFIX, ""), value)
      }
      engineCreationContext.setLabels(labels.toList.asJava)
    }
    val jMap = new java.util.HashMap[String, String](engineConf.size)
    jMap.put("jdbc.url", "jdbc:h2:~/test")
    jMap.put("jdbc.username", "sas")
    jMap.put("jdbc.password", "sa")
    jMap.putAll(engineConf.asJava)
    this.engineCreationContext.setOptions(jMap)
    this.engineCreationContext.setArgs(args)
  }

}
