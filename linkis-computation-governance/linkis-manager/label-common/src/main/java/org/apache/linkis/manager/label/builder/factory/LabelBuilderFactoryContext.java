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
 
package org.apache.linkis.manager.label.builder.factory;

import org.apache.linkis.manager.label.builder.LabelBuilder;
import org.apache.linkis.manager.label.conf.LabelCommonConfig;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Set;

public class LabelBuilderFactoryContext {

    private static final Logger logger = LoggerFactory.getLogger(LabelBuilderFactoryContext.class);

    private static Class<? extends LabelBuilderFactory> clazz = StdLabelBuilderFactory.class;
    private static LabelBuilderFactory labelBuilderFactory = null;

    public static void register(Class<? extends LabelBuilderFactory> clazz) {
        LabelBuilderFactoryContext.clazz = clazz;
    }

    public static LabelBuilderFactory getLabelBuilderFactory() {
        if (labelBuilderFactory == null) {
            synchronized (LabelBuilderFactoryContext.class) {
                if (labelBuilderFactory == null) {
                    String className = LabelCommonConfig.LABEL_FACTORY_CLASS.acquireNew();
                    if (clazz == StdLabelBuilderFactory.class && StringUtils.isNotBlank(className)) {
                        try {
                            clazz = ClassUtils.getClass(className);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("find class + " + className + " failed!", e);
                        }
                    }
                    try {
                        labelBuilderFactory = clazz.newInstance();
                        labelBuilderInitRegister(labelBuilderFactory);
                    } catch (Throwable e) {
                        throw new RuntimeException("initial class + " + className + " failed!", e);
                    }
                }
            }
        }
        return labelBuilderFactory;
    }

    /**
     * init register label builder to org.apache.linkis.entrance.factory
     *
     * @param labelBuilderFactory
     */
    private static void labelBuilderInitRegister(LabelBuilderFactory labelBuilderFactory) {
        Reflections reflections = org.apache.linkis.common.utils.ClassUtils.reflections();

        Set<Class<? extends LabelBuilder>> allLabelBuilderClass = reflections.getSubTypesOf(LabelBuilder.class);
        if (null != allLabelBuilderClass) {
            Iterator<Class<? extends LabelBuilder>> iterator = allLabelBuilderClass.iterator();
            while (iterator.hasNext()) {
                Class<? extends LabelBuilder> next = iterator.next();
                try {
                    labelBuilderFactory.registerLabelBuilder(next.newInstance());
                    logger.info("Succeed  to register label builder: " + next.getName());
                } catch (Throwable e) {
                    logger.error("Failed to register label builder: " + next.getName(), e);
                }
            }
        }
    }

}
