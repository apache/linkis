/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cs.client.test.restful;

import com.webank.wedatasphere.linkis.common.exception.ErrorException;
import com.webank.wedatasphere.linkis.cs.client.ContextClient;
import com.webank.wedatasphere.linkis.cs.client.builder.ContextClientFactory;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.LinkisBMLResource;
import com.webank.wedatasphere.linkis.cs.common.entity.resource.Resource;
import com.webank.wedatasphere.linkis.cs.common.entity.source.*;

/**
 * Created by v_wbjftang on 2020/2/28.
 */
public class RestfulTest {
    /**
     * setValueByKey
     */
    ContextClient contextClient = null;

    ContextID contextID = null;

    ContextKey contextKey = null;

    ContextValue contextValue = null;

    ContextKeyValue keyValue = null;


    Resource resource = new LinkisBMLResource();

    /*@Before*/
    public void init() {
        contextClient = ContextClientFactory.getOrCreateContextClient();
        contextID = new LinkisHAWorkFlowContextID();
        contextID.setContextId("84716");

        contextKey = new CommonContextKey();
        contextKey.setContextScope(ContextScope.FRIENDLY);
        contextKey.setContextType(ContextType.ENV);
        contextKey.setKey("project1.flow1.node1.key2");

        contextValue = new CommonContextValue();
        LinkisBMLResource resource = new LinkisBMLResource();
        resource.setResourceId("dfasdfsr2456wertg");
        resource.setVersion("v000002");
        contextValue.setValue(resource);

        keyValue = new CommonContextKeyValue();
        keyValue.setContextKey(contextKey);
        keyValue.setContextValue(contextValue);

        resource.setResourceId("edtr44356-34563456");
        resource.setVersion("v000004");


    }

    /**
     * setValueByKey
     *
     * @throws ErrorException
     */
    /* @org.junit.Test*/
    public void test01() throws ErrorException {
        contextClient.update(contextID, contextKey, contextValue);
    }

    /**
     * reset
     *
     * @throws ErrorException
     */
    /* @org.junit.Test*/
    public void test02() throws ErrorException {
        contextClient.reset(contextID, contextKey);
    }

}
