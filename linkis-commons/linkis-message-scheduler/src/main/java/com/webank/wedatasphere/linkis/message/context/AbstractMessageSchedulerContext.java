/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.message.context;

import com.webank.wedatasphere.linkis.message.builder.MessageJobBuilder;
import com.webank.wedatasphere.linkis.message.parser.ImplicitParser;
import com.webank.wedatasphere.linkis.message.parser.ServiceParser;
import com.webank.wedatasphere.linkis.message.publisher.MessagePublisher;
import com.webank.wedatasphere.linkis.message.registry.AbstractImplicitRegistry;
import com.webank.wedatasphere.linkis.message.registry.AbstractServiceRegistry;
import com.webank.wedatasphere.linkis.message.scheduler.MessageScheduler;
import com.webank.wedatasphere.linkis.message.tx.TransactionManager;


public abstract class AbstractMessageSchedulerContext implements MessageSchedulerContext {

    private AbstractServiceRegistry serviceRegistry;

    private MessagePublisher messagePublisher;

    private ServiceParser serviceParser;

    private MessageScheduler messageScheduler;

    private MessageJobBuilder messageJobBuilder;

    private TransactionManager txManager;

    private AbstractImplicitRegistry implicitRegistry;

    private ImplicitParser implicitParser;

    @Override
    public MessagePublisher getPublisher() {
        return this.messagePublisher;
    }

    public void setPublisher(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @Override
    public AbstractServiceRegistry getServiceRegistry() {
        return this.serviceRegistry;
    }

    public void setServiceRegistry(AbstractServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setserviceParser(ServiceParser serviceParser) {
        this.serviceParser = serviceParser;
    }

    public void setImplicitRegistry(AbstractImplicitRegistry implicitRegistry) {
        this.implicitRegistry = implicitRegistry;
    }

    public ImplicitParser getImplicitParser() {
        return implicitParser;
    }

    public void setImplicitParser(ImplicitParser implicitParser) {
        this.implicitParser = implicitParser;
    }

    public AbstractImplicitRegistry getImplicitRegistry() {
        return this.implicitRegistry;
    }

    public ServiceParser getservieParser() {
        return this.serviceParser;
    }

    public void setScheduler(MessageScheduler messageScheduler) {
        this.messageScheduler = messageScheduler;
    }

    public MessageScheduler getScheduler() {
        return this.messageScheduler;
    }

    public void setJobBuilder(MessageJobBuilder messageJobBuilder) {
        this.messageJobBuilder = messageJobBuilder;
    }

    public MessageJobBuilder getJobBuilder() {
        return this.messageJobBuilder;
    }

    public TransactionManager getTxManager() {
        return this.txManager;
    }

    public void setTxManager(TransactionManager txManager) {
        this.txManager = txManager;
    }

}
