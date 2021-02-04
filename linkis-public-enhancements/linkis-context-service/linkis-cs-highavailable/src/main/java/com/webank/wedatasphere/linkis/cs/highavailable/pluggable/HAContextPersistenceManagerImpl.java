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
package com.webank.wedatasphere.linkis.cs.highavailable.pluggable;

import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.highavailable.AbstractContextHAManager;
import com.webank.wedatasphere.linkis.cs.persistence.ContextPersistenceManager;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @Author alexyang
 * @Date 2020/2/22
 */
@Component
public class HAContextPersistenceManagerImpl implements ContextPersistenceManager {

    @Autowired
    private ContextIDPersistence contextIDPersistence;
    @Autowired
    private ContextMapPersistence contextMapPersistence;

    @Autowired
    private ContextMetricsPersistence contextMetricsPersistence;
    @Autowired
    private ContextIDListenerPersistence contextIDListenerPersistence;
    @Autowired
    private ContextKeyListenerPersistence contextKeyListenerPersistence;
    @Autowired
    private TransactionManager transactionManager;


    @Autowired
    private AbstractContextHAManager contextHAManager;

    @PostConstruct
    void init() throws CSErrorException {
        contextIDPersistence = contextHAManager.getContextHAProxy(contextIDPersistence);
        contextMapPersistence = contextHAManager.getContextHAProxy(contextMapPersistence);
        contextMetricsPersistence = contextHAManager.getContextHAProxy(contextMetricsPersistence);
        contextIDListenerPersistence = contextHAManager.getContextHAProxy(contextIDListenerPersistence);
        contextKeyListenerPersistence = contextHAManager.getContextHAProxy(contextKeyListenerPersistence);
    }

    @Override
    public ContextIDPersistence getContextIDPersistence() {
        return this.contextIDPersistence;
    }

    @Override
    public ContextMapPersistence getContextMapPersistence() {
        return this.contextMapPersistence;
    }





    @Override
    public ContextMetricsPersistence getContextMetricsPersistence() {
        return this.contextMetricsPersistence;
    }

    @Override
    public ContextIDListenerPersistence getContextIDListenerPersistence() {
        return this.contextIDListenerPersistence;
    }

    @Override
    public ContextKeyListenerPersistence getContextKeyListenerPersistence() {
        return this.contextKeyListenerPersistence;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

}
