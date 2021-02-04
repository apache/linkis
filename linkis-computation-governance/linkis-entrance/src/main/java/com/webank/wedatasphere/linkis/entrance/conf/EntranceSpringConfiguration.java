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

package com.webank.wedatasphere.linkis.entrance.conf;

import com.webank.wedatasphere.linkis.entrance.EntranceContext;
import com.webank.wedatasphere.linkis.entrance.EntranceParser;
import com.webank.wedatasphere.linkis.entrance.annotation.*;
import com.webank.wedatasphere.linkis.entrance.background.BackGroundService;
import com.webank.wedatasphere.linkis.entrance.event.EntranceEvent;
import com.webank.wedatasphere.linkis.entrance.event.EntranceEventListener;
import com.webank.wedatasphere.linkis.entrance.event.EntranceEventListenerBus;
import com.webank.wedatasphere.linkis.entrance.execute.*;
import com.webank.wedatasphere.linkis.entrance.execute.impl.*;
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor;
import com.webank.wedatasphere.linkis.entrance.interceptor.impl.*;
import com.webank.wedatasphere.linkis.entrance.log.*;
import com.webank.wedatasphere.linkis.entrance.parser.CommonEntranceParser;
import com.webank.wedatasphere.linkis.entrance.persistence.*;
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceGroupFactory;
import com.webank.wedatasphere.linkis.entrance.scheduler.EntranceSchedulerContext;
import com.webank.wedatasphere.linkis.rpc.ReceiverChooser;
import com.webank.wedatasphere.linkis.scheduler.Scheduler;
import com.webank.wedatasphere.linkis.scheduler.SchedulerContext;
import com.webank.wedatasphere.linkis.scheduler.queue.ConsumerManager;
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelConsumerManager;
import com.webank.wedatasphere.linkis.scheduler.queue.parallelqueue.ParallelScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;

import static com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration.ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS;

/**
 * created by enjoyyin on 2018/10/16
 * Description:This configuration class is used to generate some singleton classes in the entity module.(该配置类用于生成entrance模块中的一些单例类)
 */
@Configuration
public class EntranceSpringConfiguration {

    private Logger logger = LoggerFactory.getLogger(getClass());
    {
        logger.info("load the ujes-entrance spring configuration.");
    }

    @EntranceParserBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceParserBeanAnnotation.BEAN_NAME})
    public EntranceParser generateEntranceParser(){
        return new CommonEntranceParser();
    }

    @PersistenceEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceEngineBeanAnnotation.BEAN_NAME})
    public PersistenceEngine generatePersistenceEngine(){
        return new QueryPersistenceEngine();
    }

    @ResultSetEngineBeanAnnotation
    @ConditionalOnMissingBean(name = {ResultSetEngineBeanAnnotation.BEAN_NAME})
    public ResultSetEngine generateResultSetEngine(){
        return new EntranceResultSetEngine();
    }

    @PersistenceManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {PersistenceManagerBeanAnnotation.BEAN_NAME})
    public PersistenceManager generatePersistenceManager(@PersistenceEngineBeanAnnotation.PersistenceEngineAutowiredAnnotation PersistenceEngine persistenceEngine,
                                                         @ResultSetEngineBeanAnnotation.ResultSetEngineAutowiredAnnotation ResultSetEngine resultSetEngine){
        QueryPersistenceManager persistenceManager = new QueryPersistenceManager();
        persistenceManager.setPersistenceEngine(persistenceEngine);
        persistenceManager.setResultSetEngine(resultSetEngine);
        return persistenceManager;
    }


    @EntranceListenerBusBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceListenerBusBeanAnnotation.BEAN_NAME})
    public EntranceEventListenerBus<EntranceEventListener, EntranceEvent> generateEntranceEventListenerBus() {
        EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus = new EntranceEventListenerBus<EntranceEventListener, EntranceEvent>();
        entranceEventListenerBus.start();
        return entranceEventListenerBus;
    }

    /**
     * Update by peaceWong add CSEntranceInterceptor
     *
     * @return
     */
    @EntranceInterceptorBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceInterceptorBeanAnnotation.BEAN_NAME})
    public EntranceInterceptor[] generateEntranceInterceptors() {
        return new EntranceInterceptor[]{new CSEntranceInterceptor(),  new PythonCodeCheckInterceptor(), new DBInfoCompleteInterceptor(), new SparkCodeCheckInterceptor(),
                new SQLCodeCheckInterceptor(), new VarSubstitutionInterceptor(), new LogPathCreateInterceptor(),
                new StorePathEntranceInterceptor(), new ScalaCodeInterceptor(), new SQLLimitEntranceInterceptor(), new CommentInterceptor(),
               };
    }

    @ErrorCodeListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeListenerBeanAnnotation.BEAN_NAME})
    public ErrorCodeListener generateErrorCodeListener(@PersistenceManagerBeanAnnotation.PersistenceManagerAutowiredAnnotation PersistenceManager persistenceManager,
                                                       @EntranceParserBeanAnnotation.EntranceParserAutowiredAnnotation EntranceParser entranceParser) {
        PersistenceErrorCodeListener errorCodeListener = new PersistenceErrorCodeListener();
        errorCodeListener.setEntranceParser(entranceParser);
        errorCodeListener.setPersistenceManager(persistenceManager);
        return errorCodeListener;
    }

    @ErrorCodeManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ErrorCodeManagerBeanAnnotation.BEAN_NAME})
    public ErrorCodeManager generateErrorCodeManager() {
        return FixedErrorCodeManager$.MODULE$;
    }

    @LogManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {LogManagerBeanAnnotation.BEAN_NAME})
    public LogManager generateLogManager(@ErrorCodeListenerBeanAnnotation.ErrorCodeListenerAutowiredAnnotation ErrorCodeListener errorCodeListener,
                                         @ErrorCodeManagerBeanAnnotation.ErrorCodeManagerAutowiredAnnotation ErrorCodeManager errorCodeManager){
        CacheLogManager logManager = new CacheLogManager();
        logManager.setErrorCodeListener(errorCodeListener);
        logManager.setErrorCodeManager(errorCodeManager);
        return logManager;
    }


    @GroupFactoryBeanAnnotation
    @ConditionalOnMissingBean(name = {GroupFactoryBeanAnnotation.BEAN_NAME})
    public GroupFactory generateGroupFactory(){
        return new EntranceGroupFactory();
    }

    @ConsumerManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {ConsumerManagerBeanAnnotation.BEAN_NAME})
    public ConsumerManager generateConsumerManager(){
        return new ParallelConsumerManager(ENTRANCE_SCHEDULER_MAX_PARALLELISM_USERS().getValue());
    }

    @SchedulerContextBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerContextBeanAnnotation.BEAN_NAME})
    public SchedulerContext generateSchedulerContext(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory,
                                                     @EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation EntranceExecutorManager executorManager,
                                                     @ConsumerManagerBeanAnnotation.ConsumerManagerAutowiredAnnotation ConsumerManager consumerManager) {
        return new EntranceSchedulerContext(groupFactory, consumerManager, executorManager);
    }

    @EngineRequesterBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineRequesterBeanAnnotation.BEAN_NAME})
    public EngineRequester generateEngineRequester(){
        return new EngineRequesterImpl();
    }

    @EngineSelectorBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineSelectorBeanAnnotation.BEAN_NAME})
    public EngineSelector generateEngineSelector(@EntranceListenerBusBeanAnnotation.EntranceListenerBusAutowiredAnnotation
                                                             EntranceEventListenerBus<EntranceEventListener, EntranceEvent> entranceEventListenerBus) {
        SingleEngineSelector singleEngineSelector = new SingleEngineSelector();
        singleEngineSelector.setEntranceEventListenerBus(entranceEventListenerBus);
        return singleEngineSelector;
    }

    @EngineBuilderBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineBuilderBeanAnnotation.BEAN_NAME})
    public EngineBuilder generateEngineBuilder(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory) {
        return new AbstractEngineBuilder(groupFactory) {
            @Override
            public EntranceEngine createEngine(long id) {
                return new SingleEntranceEngine(id);
            }
        };
    }

    @EngineManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {EngineManagerBeanAnnotation.BEAN_NAME})
    public EngineManager generateEngineManager() {
        return new EngineManagerImpl();
    }

    @EntranceExecutorManagerBeanAnnotation
    @ConditionalOnMissingBean(name = {EntranceExecutorManagerBeanAnnotation.BEAN_NAME})
    public EntranceExecutorManager generateExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation GroupFactory groupFactory,
                                                   @EngineBuilderBeanAnnotation.EngineBuilderAutowiredAnnotation EngineBuilder engineBuilder,
                                                   @EngineRequesterBeanAnnotation.EngineRequesterAutowiredAnnotation EngineRequester engineRequester,
                                                   @EngineSelectorBeanAnnotation.EngineSelectorAutowiredAnnotation EngineSelector engineSelector,
                                                   @EngineManagerBeanAnnotation.EngineManagerAutowiredAnnotation EngineManager engineManager,
                                                   @Autowired EntranceExecutorRuler[] entranceExecutorRulers){
        return new EntranceExecutorManagerImpl(groupFactory, engineBuilder,
                engineRequester, engineSelector, engineManager, entranceExecutorRulers);
    }

    @SchedulerBeanAnnotation
    @ConditionalOnMissingBean(name = {SchedulerBeanAnnotation.BEAN_NAME})
    public Scheduler generateScheduler(@SchedulerContextBeanAnnotation.SchedulerContextAutowiredAnnotation SchedulerContext schedulerContext){
        Scheduler scheduler = new ParallelScheduler(schedulerContext);
        scheduler.init();
        scheduler.start();
        return scheduler;
    }

    @ReceiverChooserBeanAnnotation
    @ConditionalOnMissingBean(name = {ReceiverChooserBeanAnnotation.BEAN_NAME})
    public ReceiverChooser generateEntranceReceiverChooser(@EntranceContextBeanAnnotation.EntranceContextAutowiredAnnotation
                                                   EntranceContext entranceContext) {
        return new EntranceReceiverChooser(entranceContext);
    }

    @BackGroundServiceBeanAnnotation
    @ConditionalOnMissingBean(name = {BackGroundServiceBeanAnnotation.BEAN_NAME})
    public BackGroundService[] generateBackGroundService(){
        return new BackGroundService[]{};
    }

    @NewEngineBroadcastListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {NewEngineBroadcastListenerBeanAnnotation.BEAN_NAME})
    public NewEngineBroadcastListener generateNewEngineBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                                 EntranceExecutorManager entranceExecutorManager) {
        NewEngineBroadcastListener newEngineBroadcastListener = new NewEngineBroadcastListener();
        newEngineBroadcastListener.setEntranceExecutorManager(entranceExecutorManager);
        return newEngineBroadcastListener;
    }

    @ResponseEngineStatusChangedBroadcastListenerBeanAnnotation
    @ConditionalOnMissingBean(name = {ResponseEngineStatusChangedBroadcastListenerBeanAnnotation.BEAN_NAME})
    public ResponseEngineStatusChangedBroadcastListener generateResponseEngineStatusChangedBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                                 EntranceExecutorManager entranceExecutorManager) {
        ResponseEngineStatusChangedBroadcastListener broadcastListener = new ResponseEngineStatusChangedBroadcastListener();
        broadcastListener.setEntranceExecutorManager(entranceExecutorManager);
        return broadcastListener;
    }
}
