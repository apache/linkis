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
package com.webank.wedatasphere.linkis.cs.client.utils;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.SerializationHelper;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.ContextKeyValueBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * created by cooperyang on 2020/2/23
 * Description:
 */
public class SerializeHelper {


    private static final Logger LOGGER = LoggerFactory.getLogger(SerializeHelper.class);

    public static final SerializationHelper SERIALIZE_HELPER = ContextSerializationHelper.getInstance();

    public static String serializeContextID(ContextID contextID) throws ErrorException{
        return SERIALIZE_HELPER.serialize(contextID);
    }

    public static ContextID deserializeContextID(String contextIDStr) throws ErrorException{
        return (ContextID) SERIALIZE_HELPER.deserialize(contextIDStr);
    }



    public static String serializeContextKey(ContextKey contextKey) throws ErrorException {
        return SERIALIZE_HELPER.serialize(contextKey);
    }



    public static ContextKey deserializeContextKey(String contextKeyStr) throws ErrorException{
        return (ContextKey)SERIALIZE_HELPER.deserialize(contextKeyStr);
    }

    public static String serializeContextValue(ContextValue contextValue) throws ErrorException{
        return SERIALIZE_HELPER.serialize(contextValue);
    }

    public static ContextValue deserializeContextValue(String contextValueStr) throws ErrorException{
        return (ContextValue)SERIALIZE_HELPER.deserialize(contextValueStr);
    }

    public static ContextKeyValueBean deserializeContextKVBean(String contextKVBeanStr) throws ErrorException{
        return (ContextKeyValueBean)SERIALIZE_HELPER.deserialize(contextKVBeanStr);
    }

    public static ContextKeyValue deserializeContextKeyValue(String contextKVStr) throws ErrorException{
        return (ContextKeyValue)SERIALIZE_HELPER.deserialize(contextKVStr);
    }



}
