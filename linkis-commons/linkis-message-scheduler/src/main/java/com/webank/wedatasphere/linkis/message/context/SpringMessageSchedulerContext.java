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

import com.webank.wedatasphere.linkis.message.builder.DefaultMessageJobBuilder;
import com.webank.wedatasphere.linkis.message.parser.DefaultImplicitParser;
import com.webank.wedatasphere.linkis.message.parser.DefaultServiceParser;
import com.webank.wedatasphere.linkis.message.publisher.DefaultMessagePublisher;
import com.webank.wedatasphere.linkis.message.registry.SpringImplicitRegistry;
import com.webank.wedatasphere.linkis.message.registry.SpringServiceRegistry;
import com.webank.wedatasphere.linkis.message.scheduler.DefaultMessageScheduler;
import com.webank.wedatasphere.linkis.message.tx.SpringTransactionManager;


public class SpringMessageSchedulerContext extends AbstractMessageSchedulerContext {

    public SpringMessageSchedulerContext(){
        setImplicitParser(new DefaultImplicitParser());
        setImplicitRegistry(new SpringImplicitRegistry(this));
        setserviceParser(new DefaultServiceParser());
        setPublisher(new DefaultMessagePublisher(this));
        setServiceRegistry(new SpringServiceRegistry(this));
        setScheduler(new DefaultMessageScheduler());
        setJobBuilder(new DefaultMessageJobBuilder());
        setTxManager(new SpringTransactionManager());
    }

}
