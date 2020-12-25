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
package com.webank.wedatasphere.linkis.cs.common.serialize.helper;

import com.webank.wedatasphere.linkis.cs.common.serialize.ContextSerializer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author peacewong
 * @date 2020/2/27 19:14
 */
public class ContextSerializationHelper extends AbstractSerializationHelper {


    private static final Logger logger = LoggerFactory.getLogger(ContextSerializationHelper.class);

    private Map<String, ContextSerializer> contextSerializerMap = new HashMap<>(16);


    private void init()  {
        Reflections reflections = new Reflections("com.webank.wedatasphere.linkis", ContextSerializationHelper.class.getClassLoader());
        Set<Class<? extends ContextSerializer>> allSubClass = reflections.getSubTypesOf(ContextSerializer.class);

        if ( null != allSubClass){
            Iterator<Class<? extends ContextSerializer>> iterator = allSubClass.iterator();
            while (iterator.hasNext()){
                Class<? extends ContextSerializer> next = iterator.next();
                ContextSerializer contextSerializer = null;
                try {
                    contextSerializer = next.newInstance();
                } catch (InstantiationException e) {
                    logger.info("Failed to Instantiation  " + next.getName());
                    continue;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to construct contextSerializer", e);
                }

                if (contextSerializerMap.containsKey(contextSerializer.getType())){
                    throw new RuntimeException("contextSerializer Type cannot be duplicated ");
                }
                contextSerializerMap.put(contextSerializer.getType(), contextSerializer);
            }
        }
    }



    private static ContextSerializationHelper contextSerializationHelper = null;

    public static ContextSerializationHelper getInstance() {
        if (contextSerializationHelper == null) {
            synchronized (ContextSerializationHelper.class) {
                if (contextSerializationHelper == null) {
                    contextSerializationHelper = new ContextSerializationHelper();
                    contextSerializationHelper.init();
                }
            }
        }
        return contextSerializationHelper;
    }

    @Override
    protected Map<String, ContextSerializer> getContextSerializerMap() {
        return this.contextSerializerMap;
    }
}
