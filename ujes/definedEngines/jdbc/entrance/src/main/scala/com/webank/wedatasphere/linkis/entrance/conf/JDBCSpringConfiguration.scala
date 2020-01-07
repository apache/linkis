/*
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
package com.webank.wedatasphere.linkis.entrance.conf

import com.webank.wedatasphere.linkis.entrance.EntranceParser
import com.webank.wedatasphere.linkis.entrance.annotation._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.executer.JDBCEngineExecutorManagerImpl
import com.webank.wedatasphere.linkis.entrance.parser.JDBCEntranceParser
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Configuration

@Configuration
class JDBCSpringConfiguration {


  private val logger = LoggerFactory.getLogger(classOf[JDBCSpringConfiguration])

  @EntranceExecutorManagerBeanAnnotation
  def generateEntranceExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory,
                                      @EngineBuilderBeanAnnotation.EngineBuilderAutowiredAnnotation engineBuilder: EngineBuilder,
                                      @EngineRequesterBeanAnnotation.EngineRequesterAutowiredAnnotation engineRequester: EngineRequester,
                                      @EngineSelectorBeanAnnotation.EngineSelectorAutowiredAnnotation engineSelector: EngineSelector,
                                      @EngineManagerBeanAnnotation.EngineManagerAutowiredAnnotation engineManager: EngineManager,
                                      @Autowired entranceExecutorRulers: Array[EntranceExecutorRuler]): EntranceExecutorManager =
    new JDBCEngineExecutorManagerImpl(groupFactory, engineBuilder, engineRequester, engineSelector, engineManager, entranceExecutorRulers)




  @EntranceParserBeanAnnotation
  def generateEntranceParser():EntranceParser = {
    logger.info("begin to get JDBC Entrance parser")
    new JDBCEntranceParser()
  }

}
