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

package com.webank.wedatasphere.linkis.entranceclient.conf;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.entrance.annotation.EntranceExecutorManagerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.annotation.NewEngineBroadcastListenerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.annotation.ResponseEngineStatusChangedBroadcastListenerBeanAnnotation;
import com.webank.wedatasphere.linkis.entrance.conf.EntranceConfiguration;
import com.webank.wedatasphere.linkis.entrance.conf.EntranceSpringConfiguration;
import com.webank.wedatasphere.linkis.entrance.execute.*;
import com.webank.wedatasphere.linkis.entrance.execute.impl.ConcurrentEngineSelector;
import com.webank.wedatasphere.linkis.entrance.interceptor.EntranceInterceptor;
import com.webank.wedatasphere.linkis.entranceclient.EntranceClient;
import com.webank.wedatasphere.linkis.entranceclient.EntranceClientImpl;
import com.webank.wedatasphere.linkis.entranceclient.annotation.*;
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEngineBuilderBeanAnnotation.ClientEngineBuilderAutowiredAnnotation;
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientEngineRequesterBeanAnnotation.ClientEngineRequesterAutowiredAnnotation;
import com.webank.wedatasphere.linkis.entranceclient.annotation.ClientGroupFactoryBeanAnnotation.ClientGroupFactoryAutowiredAnnotation;
import com.webank.wedatasphere.linkis.entranceclient.context.ClientEntranceParser;
import com.webank.wedatasphere.linkis.entranceclient.execute.ClientEngineBuilder;
import com.webank.wedatasphere.linkis.entranceclient.execute.ClientEngineRequester;
import com.webank.wedatasphere.linkis.entranceclient.scheduler.ClientGroupFactory;
import com.webank.wedatasphere.linkis.rpc.ClientNewEngineBroadcastListener;
import com.webank.wedatasphere.linkis.rpc.ClientResponseEngineStatusChangedBroadcastListener;
import com.webank.wedatasphere.linkis.rpc.RPCReceiveRestful;
import com.webank.wedatasphere.linkis.rpc.conf.RPCConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
  * Created by johnnwang on 2018/11/3.
  */
@Configuration
@AutoConfigureBefore({EntranceSpringConfiguration.class, RPCReceiveRestful.class})
class ClientSpringConfiguration {

  private Logger logger = LoggerFactory.getLogger(getClass());
  {
    logger.info("start a multi-entrance application...");
  }

    @ClientEntranceParserBeanAnnotation
    @ConditionalOnMissingBean(name = {ClientEntranceParserBeanAnnotation.BEAN_NAME})
    public ClientEntranceParser generateClientEntranceParser() {
        return new ClientEntranceParser();
    }

  @ClientGroupFactoryBeanAnnotation
  @ConditionalOnMissingBean(name = {ClientGroupFactoryBeanAnnotation.BEAN_NAME})
  public ClientGroupFactory generateClientGroupFactory() {
    return new ClientGroupFactory();
  }

  @ClientEngineBuilderBeanAnnotation
  @ConditionalOnMissingBean(name = {ClientEngineBuilderBeanAnnotation.BEAN_NAME})
  public ClientEngineBuilder generateClientEngineBuilder(@ClientGroupFactoryAutowiredAnnotation ClientGroupFactory clientGroupFactory) {
    return new ClientEngineBuilder(clientGroupFactory);
  }


  @ClientEngineRequesterBeanAnnotation
  @ConditionalOnMissingBean(name = {ClientEngineRequesterBeanAnnotation.BEAN_NAME})
  public ClientEngineRequester generateClientEngineRequester() {
    return new ClientEngineRequester();
  }

  @ClientEngineSelectorBeanAnnotation
  @ConditionalOnMissingBean(name = {ClientEngineSelectorBeanAnnotation.BEAN_NAME})
  public EngineSelector generateClientEngineSelector() {
    setClientValue(EntranceConfiguration.CONCURRENT_ENGINE_MAX_PARALLELISM(), ClientConfiguration.CONCURRENT_ENGINE_MAX_PARALLELISM_FOR_CLIENT());
      return new ConcurrentEngineSelector();
  }

  @ClientInterceptorsBeanAnnotation
  @ConditionalOnMissingBean(name = {ClientInterceptorsBeanAnnotation.BEAN_NAME})
  public EntranceInterceptor[] generateEntranceInterceptor() {
    return new EntranceInterceptor[0];
  }

  @NewEngineBroadcastListenerBeanAnnotation
  @Conditional(MultiEntranceCondition.class)
  public NewEngineBroadcastListener generateNewEngineBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                               EntranceExecutorManager entranceExecutorManager) {
    NewEngineBroadcastListener newEngineBroadcastListener = new ClientNewEngineBroadcastListener();
    newEngineBroadcastListener.setEntranceExecutorManager(entranceExecutorManager);
    return newEngineBroadcastListener;
  }

  @ResponseEngineStatusChangedBroadcastListenerBeanAnnotation
  @Conditional(MultiEntranceCondition.class)
  public ResponseEngineStatusChangedBroadcastListener generateResponseEngineStatusChangedBroadcastListener(@EntranceExecutorManagerBeanAnnotation.EntranceExecutorManagerAutowiredAnnotation
                                                                                                                   EntranceExecutorManager entranceExecutorManager) {
    ResponseEngineStatusChangedBroadcastListener broadcastListener = new ClientResponseEngineStatusChangedBroadcastListener();
    broadcastListener.setEntranceExecutorManager(entranceExecutorManager);
    return broadcastListener;
  }

  @DefaultEntranceClientBeanAnnotation
  @Conditional(MultiEntranceCondition.class)
  public EntranceClient generateEntranceClient(@ClientEntranceParserBeanAnnotation.ClientEntranceParserAutowiredAnnotation ClientEntranceParser clientEntranceParser,
                                           @ClientGroupFactoryAutowiredAnnotation ClientGroupFactory clientGroupFactory,
                                           @ClientInterceptorsBeanAnnotation.ClientInterceptorsAutowiredAnnotation EntranceInterceptor[] clientInterceptors,
                                           @ClientEngineBuilderAutowiredAnnotation ClientEngineBuilder clientEngineBuilder,
                                           @ClientEngineSelectorBeanAnnotation.ClientEngineSelectorAutowiredAnnotation EngineSelector clientEngineSelector,
                                           @ClientEngineRequesterAutowiredAnnotation ClientEngineRequester clientEngineRequester,
                                           @Autowired EntranceExecutorRuler[] entranceExecutorRulers) {
    logger.warn("try to check the rpc receiver consumer threadPool...");
      setClientValue(RPCConfiguration.BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY(), ClientConfiguration.BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY_FOR_CLIENT());
    setClientValue(RPCConfiguration.BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX(), ClientConfiguration.BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX_FOR_CLIENT());
      EntranceClientImpl client = EntranceClientImpl.apply(ClientConfiguration.CLIENT_DEFAULT_NAME());
    logger.warn("Multi-entrance application is ready to initial EntranceClient " + client.getEntranceClientName());
    client.init(clientEntranceParser, clientGroupFactory, clientEngineBuilder, clientEngineRequester,
      clientEngineSelector, clientInterceptors, entranceExecutorRulers,
      ClientConfiguration.CLIENT_DEFAULT_PARALLELISM_USERS().getValue());
    return client;
  }

//  @EventListener
//  public void onApplicationStarting(ApplicationStartingEvent applicationStartingEvent) {
//    if(MultiEntranceCondition.isMultiEntranceApplication()) {
//      logger.warn("start a multi-entrance application, now try to check the rpc receiver consumer threadPool...");
//      setClientValue(RPCConfiguration.BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY(), ClientConfiguration.BDP_RPC_RECEIVER_ASYN_QUEUE_CAPACITY_FOR_CLIENT());
//      setClientValue(RPCConfiguration.BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX(), ClientConfiguration.BDP_RPC_RECEIVER_ASYN_CONSUMER_THREAD_MAX_FOR_CLIENT());
//      setClientValue(EntranceConfiguration.CONCURRENT_ENGINE_MAX_PARALLELISM(), ClientConfiguration.CONCURRENT_ENGINE_MAX_PARALLELISM_FOR_CLIENT());
//    }
//  }

  private void setClientValue(CommonVars<?> fromConf, CommonVars<Integer> toConf) {
    int queueSize = Integer.parseInt(fromConf.getValue().toString());
    int queueSizeValue = toConf.getValue();
    if(queueSize < queueSizeValue) {
      String key = fromConf.key();
      DataWorkCloudApplication.setProperty(key, String.valueOf(queueSizeValue));
      logger.warn("Multi-entrance application set " + key + "=" + queueSizeValue);
    }
  }
}