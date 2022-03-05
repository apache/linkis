/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.conf;

import org.apache.linkis.entrance.EntranceParser;
import org.apache.linkis.entrance.annotation.*;
import org.apache.linkis.entrance.cli.heartbeat.CliHeartbeatMonitor;
import org.apache.linkis.entrance.cli.heartbeat.KillHandler;
import org.apache.linkis.entrance.event.*;
import org.apache.linkis.entrance.execute.impl.EntranceExecutorManagerImpl;
import org.apache.linkis.entrance.interceptor.EntranceInterceptor;
import org.apache.linkis.entrance.interceptor.OnceJobInterceptor;
import org.apache.linkis.entrance.interceptor.impl.*;
import org.apache.linkis.entrance.log.*;
import org.apache.linkis.entrance.parser.CommonEntranceParser;
import org.apache.linkis.entrance.persistence.*;
import org.apache.linkis.entrance.scheduler.EntranceGroupFactory;
import org.apache.linkis.entrance.scheduler.EntranceSchedulerContext;
import org.apache.linkis.orchestrator.ecm.EngineConnManagerBuilder;
import org.apache.linkis.orchestrator.ecm.EngineConnManagerBuilder$;
import org.apache.linkis.orchestrator.ecm.entity.Policy;
import org.apache.linkis.scheduler.Scheduler;
import org.apache.linkis.scheduler.SchedulerContext;
import org.apache.linkis.scheduler.executer.ExecutorManager;
import org.apache.linkis.scheduler.queue.ConsumerManager;
import org.apache.linkis.scheduler.queue.GroupFactory;
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager;
import org.apache.linkis.scheduler.queue.parallelqueue.ParallelScheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.entrance.conf.EntranceConfiguration.ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS;

/**
 * Description:This configuration class is used to generate some singleton classes in the entity
 * module.(该配置类用于生成entrance模块中的一些单例类)
 */
@Configuration
// @AutoConfigureBefore({EntranceServer.class, EntranceExecutionService.class})
public class EntranceSpringConfiguration {

    private Logger logger = LoggerFactory.getLogger(getClass());

    {
        logger.info("load the linkis-cg-entrance spring configuration.");
    }

    @PersistenceEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceEngineBeanAnnotation.BEAN_NAME})
    public PersistenceEngine generatePersistenceEngine() {
        return new QueryPersistenceEngine();
    }

    @ResultSetEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {ResultSetEngineBeanAnnotation.BEAN_NAME})
    public ResultSetEngine generateResultSetEngine() {
        return new EntranceResultSetEngine();
    }

    @CliHeartBeatMonitorAnnotation
    @ConditionalOnMissingBean(name = {CliHeartBeatMonitorAnnotation.BEAN_NAME})
    public CliHeartbeatMonitor generateCliHeartbeatMonitor() {
        CliHeartbeatMonitor cliHeartbeatMonitor = new CliHeartbeatMonitor(new KillHandler());
        cliHeartbeatMonitor.start();
        return cliHeartbeatMonitor;
    }

    @PersistenceManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceManagerBeanAnnotation.BEAN_NAME})
    public PersistenceManager generatePersistenceManager(
            @PersistenceEngineBeanAnnotation.PersistenceEngineAutowiredAnnotation
                    PersistenceEngine persistenceEngine,
            @ResultSetEngineBeanAnnotation.ResultSetEngineAutowiredAnnotation
                    ResultSetEngine resultSetEngine,
            @CliHeartBeatMonitorAnnotation.CliHeartBeatMonitorAutowiredAnnotation
                    CliHeartbeatMonitor cliHeartbeatMonitor) {
        logger.info("init PersistenceManager.");
        QueryPersistenceManager persistenceManager = new QueryPersistenceManager();
        persistenceManager.setPersistenceEngine(persistenceEngine);
        persistenceManager.setResultSetEngine(resultSetEngine);
        persistenceManager.setCliHeartbeatMonitor(cliHeartbeatMonitor);
        return persistenceManager;
    }

    @EntranceParserBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceParserBeanAnnotation.BEAN_NAME})
    public EntranceParser generateEntranceParser(
            @PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation
                    PersistenceManager persistenceManager) {
        return new CommonEntranceParser(persistenceManager);
    }

    @EntranceListenerBusBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceListenerBusBeanAnnotation.BEAN_NAME})
    public EntranceEventListenerBus<EntranceEventListener, EntranceEvent>
            generateEntranceEventListenerBus() {
        EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus =
                new EntranceEventListenerBus<EntranceEventListener, EntranceEvent>();
        entranceEventListenerBus.start();
        return entranceEventListenerBus;
    }

    /**
     * add CSEntranceInterceptor
     *
     * @return
     */
    @EntranceInterceptorBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceInterceptorBeanAnnotation.BEAN_NAME})
    public EntranceInterceptor[] generateEntranceInterceptors() {
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
            new CommentInterceptor()
        };
    }

    @ErrorCodeListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeListenerBeanAnnotation.BEAN_NAME})
    public ErrorCodeListener generateErrorCodeListener(
            @PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation
                    PersistenceManager persistenceManager,
            @EntranceParserBeanAnnotation.EntranceParserAutowiredAnnotation
                    EntranceParser entranceParser) {
        PersistenceErrorCodeListener errorCodeListener = new PersistenceErrorCodeListener();
        errorCodeListener.setEntranceParser(entranceParser);
        errorCodeListener.setPersistenceManager(persistenceManager);
        return errorCodeListener;
    }

    @ErrorCodeManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeManagerBeanAnnotation.BEAN_NAME})
    public ErrorCodeManager generateErrorCodeManager() {
        /* try {
            Class.forName("org.apache.linkis.errorcode.client.handler.LinkisErrorCodeHandler");
        } catch (final Exception e) {
            logger.error("failed to init linkis error code handler", e);
        }*/
        return FlexibleErrorCodeManager$.MODULE$;
    }

    @LogManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {LogManagerBeanAnnotation.BEAN_NAME})
    public LogManager generateLogManager(
            @ErrorCodeListenerBeanAnnotation.ErrorCodeListenerAutowiredAnnotation
                    ErrorCodeListener errorCodeListener,
            @ErrorCodeManagerBeanAnnotation.ErrorCodeManagerAutowiredAnnotation
                    ErrorCodeManager errorCodeManager) {
        CacheLogManager logManager = new CacheLogManager();
        logManager.setErrorCodeListener(errorCodeListener);
        logManager.setErrorCodeManager(errorCodeManager);
        return logManager;
    }

    @GroupFactoryBeanAnnotation
    @ConditionalOnMissingBean(name = {GroupFactoryBeanAnnotation.BEAN_NAME})
    public GroupFactory generateGroupFactory() {
        return new EntranceGroupFactory();
    }

    @ConsumerManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ConsumerManagerBeanAnnotation.BEAN_NAME})
    public ConsumerManager generateConsumerManager() {
        return new ParallelConsumerManager(
                ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS().getValue(), "EntranceJobScheduler");
    }

    @SchedulerContextBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerContextBeanAnnotation.BEAN_NAME})
    public SchedulerContext generateSchedulerContext(
            @GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory,
            @EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                    ExecutorManager executorManager,
            @ConsumerManagerBeanAnnotation.ConsumerManagerAutowiredAnnotation
                    ConsumerManager consumerManager) {
        return new EntranceSchedulerContext(groupFactory, consumerManager, executorManager);
    }

    @EntranceExecutorManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceExecutorManagerBeanAnnotation.BEAN_NAME})
    public ExecutorManager generateExecutorManager(
            @GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory) {
        EngineConnManagerBuilder engineConnManagerBuilder =
                EngineConnManagerBuilder$.MODULE$.builder();
        engineConnManagerBuilder.setPolicy(Policy.Process);
        return new EntranceExecutorManagerImpl(groupFactory, engineConnManagerBuilder.build());
    }

    @SchedulerBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerBeanAnnotation.BEAN_NAME})
    public Scheduler generateScheduler(
            @SchedulerContextBeanAnnotation.SchedulerContextAutowiredAnnotation
                    SchedulerContext schedulerContext) {
        Scheduler scheduler = new ParallelScheduler(schedulerContext);
        scheduler.init();
        scheduler.start();
        return scheduler;
    }
}
