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

package org.apache.linkis.engineplugin.trino.executer

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
import org.apache.linkis.engineplugin.trino.executor.TrinoEngineConnExecutor
import org.apache.linkis.engineplugin.trino.factory.TrinoEngineConnFactory
import org.apache.linkis.governance.common.conf.GovernanceCommonConf
import org.apache.linkis.governance.common.entity.ExecutionNodeStatus
import org.apache.linkis.governance.common.utils.EngineConnArgumentsParser
import org.apache.linkis.manager.engineplugin.common.launch.process.Environment
import org.apache.linkis.manager.label.builder.factory.{
  LabelBuilderFactory,
  LabelBuilderFactoryContext
}
import org.apache.linkis.manager.label.entity.Label

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

import org.junit.jupiter.api.Assertions

class TestTrinoEngineConnExecutor {

  private val engineCreationContext: EngineCreationContext = new DefaultEngineCreationContext

  private val labelBuilderFactory: LabelBuilderFactory =
    LabelBuilderFactoryContext.getLabelBuilderFactory

//  @Test
  def testExecuteLine: Unit = {
    val engineconnConf = "--engineconn-conf"
    val springConf = "--spring-conf"
    val array = Array(
      engineconnConf,
      "wds.linkis.rm.instance=10",
      engineconnConf,
      "label.userCreator=root-IDE",
      engineconnConf,
      "ticketId=037ab855-0c41-4323-970d-7f75e71883b6",
      engineconnConf,
      "label.engineType=trino",
      engineconnConf,
      "linkis.trino.url=https://trino.dev.com/hive/hivetest",
      engineconnConf,
      "linkis.trino.ssl.insecured=true",
      engineconnConf,
      "linkis.trino.default.start.user=root",
      engineconnConf,
      "linkis.trino.password=123456",
      springConf,
      "eureka.client.serviceUrl.defaultZone=http://127.0.0.1:8761/eureka/",
      springConf,
      "logging.config=classpath:log4j2.xml",
      springConf,
      "spring.profiles.active=engineconn",
      springConf,
      "server.port=35655",
      springConf,
      "spring.application.name=linkis-cg-engineconn"
    )
    this.init(array)
    val cmd = "SHOW SCHEMAS"
    val taskId = "1"
    val task = new CommonEngineConnTask(taskId, false)
    val properties = new util.HashMap[String, Object]
    task.setProperties(properties)
    task.data(ComputationEngineConstant.LOCK_TYPE_NAME, "lock")
    task.setStatus(ExecutionNodeStatus.Scheduled)
    val engineFactory: TrinoEngineConnFactory = new TrinoEngineConnFactory
    val engine = engineFactory.createEngineConn(engineCreationContext)

    val jdbcExecutor: TrinoEngineConnExecutor = engineFactory
      .newExecutor(1, engineCreationContext, engine)
      .asInstanceOf[TrinoEngineConnExecutor]
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
    val response = jdbcExecutor.executeLine(engineExecutionContext, cmd)
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
    jMap.putAll(engineConf.asJava)
    this.engineCreationContext.setOptions(jMap)
    this.engineCreationContext.setArgs(args)
    sys.props.asJava.putAll(jMap)
  }

}
