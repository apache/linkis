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

package org.apache.linkis.entrance.conf;

import org.apache.linkis.entrance.EntranceParser;
import org.apache.linkis.entrance.cli.heartbeat.CliHeartbeatMonitor;
import org.apache.linkis.entrance.cli.heartbeat.KillHandler;
import org.apache.linkis.entrance.constant.ServiceNameConsts;
import org.apache.linkis.entrance.event.EntranceEvent;
import org.apache.linkis.entrance.event.EntranceEventListener;
import org.apache.linkis.entrance.event.EntranceEventListenerBus;
import org.apache.linkis.entrance.execute.impl.EntranceExecutorManagerImpl;
import org.apache.linkis.entrance.interceptor.EntranceInterceptor;
import org.apache.linkis.entrance.interceptor.OnceJobInterceptor;
import org.apache.linkis.entrance.interceptor.impl.*;
import org.apache.linkis.entrance.log.CacheLogManager;
import org.apache.linkis.entrance.log.ErrorCodeListener;
import org.apache.linkis.entrance.log.ErrorCodeManager;
import org.apache.linkis.entrance.log.FlexibleErrorCodeManager$;
import org.apache.linkis.entrance.log.LogManager;
import org.apache.linkis.entrance.log.PersistenceErrorCodeListener;
import org.apache.linkis.entrance.parser.CommonEntranceParser;
import org.apache.linkis.entrance.persistence.EntranceResultSetEngine;
import org.apache.linkis.entrance.persistence.PersistenceEngine;
import org.apache.linkis.entrance.persistence.PersistenceManager;
import org.apache.linkis.entrance.persistence.QueryPersistenceEngine;
import org.apache.linkis.entrance.persistence.QueryPersistenceManager;
import org.apache.linkis.entrance.persistence.ResultSetEngine;
import org.apache.linkis.entrance.scheduler.EntranceGroupFactory;
import org.apache.linkis.entrance.scheduler.EntranceParallelConsumerManager;
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext;
import org.apache.linkis.scheduler.Scheduler;
import org.apache.linkis.scheduler.SchedulerContext;
import org.apache.linkis.scheduler.executer.ExecutorManager;
import org.apache.linkis.scheduler.queue.ConsumerManager;
import org.apache.linkis.scheduler.queue.GroupFactory;
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelScheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.entrance.conf.EntranceConfiguration.ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS;

/**
 * Description:This configuration class is used to generate some singleton classes in the entity
 * module.(该配置类用于生成entrance模块中的一些单例类)
 */
@Configuration
public class EntranceSpringConfiguration {

  private Logger logger = LoggerFactory.getLogger(getClass());

  {
    logger.info("load the linkis-cg-entrance spring configuration.");
  }

  @Bean
  @ConditionalOnMissingBean
  public PersistenceEngine persistenceEngine() {
    return new QueryPersistenceEngine();
  }

  @Bean
  @ConditionalOnMissingBean
  public ResultSetEngine resultSetEngine() {
    return new EntranceResultSetEngine();
  }

  @Bean
  @ConditionalOnMissingBean
  public CliHeartbeatMonitor cliHeartbeatMonitor() {
    CliHeartbeatMonitor cliHeartbeatMonitor = new CliHeartbeatMonitor(new KillHandler());
    cliHeartbeatMonitor.start();
    return cliHeartbeatMonitor;
  }

  @Bean
  @ConditionalOnMissingBean
  public PersistenceManager persistenceManager(
      PersistenceEngine persistenceEngine,
      ResultSetEngine resultSetEngine,
      CliHeartbeatMonitor cliHeartbeatMonitor) {
    logger.info("init PersistenceManager.");
    QueryPersistenceManager persistenceManager = new QueryPersistenceManager();
    persistenceManager.setPersistenceEngine(persistenceEngine);
    persistenceManager.setResultSetEngine(resultSetEngine);
    persistenceManager.setCliHeartbeatMonitor(cliHeartbeatMonitor);
    return persistenceManager;
  }

  @Bean
  @ConditionalOnMissingBean
  public EntranceParser entranceParser(PersistenceManager persistenceManager) {
    return new CommonEntranceParser(persistenceManager);
  }

  @Bean
  @ConditionalOnMissingBean
  public EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus() {
    EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus =
        new EntranceEventListenerBus<>();
    entranceEventListenerBus.start();
    return entranceEventListenerBus;
  }

  /**
   * add CSEntranceInterceptor
   *
   * @return
   */
  @Bean
  @ConditionalOnMissingBean(name = {ServiceNameConsts.ENTRANCE_INTERCEPTOR})
  public EntranceInterceptor[] entranceInterceptors() {
    return new EntranceInterceptor[] {
      new OnceJobInterceptor(),
      new CSEntranceInterceptor(),
      new ShellDangerousGrammerInterceptor(),
      // new PythonCodeCheckInterceptor(),
      // new DBInfoCompleteInterceptor(),
      new CompatibleInterceptor(),
      new SparkCodeCheckInterceptor(),
      new SQLCodeCheckInterceptor(),
      new LabelCheckInterceptor(),
      new ParserVarLabelInterceptor(),
      new VarSubstitutionInterceptor(),
      new LogPathCreateInterceptor(),
      new StorePathEntranceInterceptor(),
      new ScalaCodeInterceptor(),
      new SQLLimitEntranceInterceptor(),
      new CommentInterceptor(),
      //      new SetTenantLabelInterceptor(),
      new UserCreatorIPCheckInterceptor()
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public ErrorCodeListener errorCodeListener(
      PersistenceManager persistenceManager, EntranceParser entranceParser) {
    PersistenceErrorCodeListener errorCodeListener = new PersistenceErrorCodeListener();
    errorCodeListener.setEntranceParser(entranceParser);
    errorCodeListener.setPersistenceManager(persistenceManager);
    return errorCodeListener;
  }

  @Bean
  @ConditionalOnMissingBean
  public ErrorCodeManager errorCodeManager() {
    return FlexibleErrorCodeManager$.MODULE$;
  }

  @Bean
  @ConditionalOnMissingBean
  public LogManager logManager(
      ErrorCodeListener errorCodeListener, ErrorCodeManager errorCodeManager) {
    CacheLogManager logManager = new CacheLogManager();
    logManager.setErrorCodeListener(errorCodeListener);
    logManager.setErrorCodeManager(errorCodeManager);
    return logManager;
  }

  @Bean
  @ConditionalOnMissingBean
  public GroupFactory groupFactory() {
    return new EntranceGroupFactory();
  }

  @Bean
  @ConditionalOnMissingBean
  public ConsumerManager consumerManager() {
    return new EntranceParallelConsumerManager(
        ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS().getValue(), "EntranceJobScheduler");
  }

  @Bean
  @ConditionalOnMissingBean
  public SchedulerContext schedulerContext(
      GroupFactory groupFactory, ExecutorManager executorManager, ConsumerManager consumerManager) {
    return new EntranceSchedulerContext(groupFactory, consumerManager, executorManager);
  }

  @Bean
  @ConditionalOnMissingBean
  public ExecutorManager executorManager(GroupFactory groupFactory) {
    return new EntranceExecutorManagerImpl(groupFactory);
  }

  @Bean
  @ConditionalOnMissingBean
  public Scheduler scheduler(SchedulerContext schedulerContext) {
    Scheduler scheduler = new ParallelScheduler(schedulerContext);
    scheduler.init();
    scheduler.start();
    return scheduler;
  }
}
