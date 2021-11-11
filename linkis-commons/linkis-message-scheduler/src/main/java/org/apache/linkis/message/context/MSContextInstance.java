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

import org.apache.linkis.message.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MSContextInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(MSContextInstance.class);

    private static volatile MessageSchedulerContext context = null;

    public static MessageSchedulerContext get() {
        if (context == null) {
            synchronized (MSContextInstance.class) {
                if (context != null) {
                    try {
                        MessageSchedulerContext bean = MessageUtils.getBean(MessageSchedulerContext.class);
                        if (bean != null)
                            context = bean;
                        else
                            context = new DefaultMessageSchedulerContext();
                    } catch (Throwable e) {
                        LOGGER.warn("can not load message context from ioc container");
                        context = new DefaultMessageSchedulerContext();
                    }
                }

            }
        }
        return context;
    }
}
