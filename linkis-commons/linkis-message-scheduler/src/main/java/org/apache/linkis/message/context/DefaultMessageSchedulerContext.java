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
 
package org.apache.linkis.message.context;

import org.apache.linkis.message.builder.DefaultMessageJobBuilder;
import org.apache.linkis.message.parser.DefaultImplicitParser;
import org.apache.linkis.message.parser.DefaultServiceParser;
import org.apache.linkis.message.publisher.DefaultMessagePublisher;
import org.apache.linkis.message.registry.AbstractImplicitRegistry;
import org.apache.linkis.message.registry.AbstractServiceRegistry;
import org.apache.linkis.message.scheduler.DefaultMessageScheduler;
import org.apache.linkis.message.tx.TransactionManager;


public class DefaultMessageSchedulerContext extends AbstractMessageSchedulerContext {

    {
        setImplicitParser(new DefaultImplicitParser());
        setImplicitRegistry(new AbstractImplicitRegistry(this){});
        setserviceParser(new DefaultServiceParser());
        setPublisher(new DefaultMessagePublisher(this));
        setServiceRegistry(new AbstractServiceRegistry(this) {
        });
        setScheduler(new DefaultMessageScheduler());
        setJobBuilder(new DefaultMessageJobBuilder());
        setTxManager(new TransactionManager() {
        });
    }

}
