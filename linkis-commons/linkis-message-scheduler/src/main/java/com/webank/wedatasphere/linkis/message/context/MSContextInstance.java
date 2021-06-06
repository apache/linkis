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

import com.webank.wedatasphere.linkis.common.utils.Utils;
import com.webank.wedatasphere.linkis.message.utils.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.runtime.AbstractFunction0;
import scala.runtime.AbstractFunction1;

/**
 * @date 2020/9/17
 */
public class MSContextInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(MSContextInstance.class);

    private static volatile MessageSchedulerContext context = null;

    public static MessageSchedulerContext get() {
        if (context == null) {
            synchronized (MSContextInstance.class) {
                if (context != null) {

                    context = Utils.tryCatch(new AbstractFunction0<MessageSchedulerContext>() {
                       @Override
                       public MessageSchedulerContext apply() {
                           MessageSchedulerContext bean = MessageUtils.getBean(MessageSchedulerContext.class);
                           if (bean != null)
                            context = bean;
                           else
                            context = new DefaultMessageSchedulerContext();
                           return context;
                       }
                   }, new AbstractFunction1<Throwable, MessageSchedulerContext>() {
                       @Override
                       public MessageSchedulerContext apply(Throwable v1) {
                           LOGGER.warn("can not load message context from ioc container");
                           context = new DefaultMessageSchedulerContext();
                           return context;
                       }
                   });
                }

            }
        }
        return context;
    }
}
