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
package com.webank.wedatasphere.linkis.cs.persistence;

import com.webank.wedatasphere.linkis.cs.persistence.annotation.Tuning;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.*;

/**
 * Created by patinousward on 2020/2/17.
 */
public class ContextPersistenceManagerImpl implements ContextPersistenceManager {


    private ContextIDPersistence contextIDPersistence;

    private ContextMapPersistence contextMapPersistence;



    private ContextMetricsPersistence contextMetricsPersistence;

    private ContextIDListenerPersistence contextIDListenerPersistence;

    private ContextKeyListenerPersistence contextKeyListenerPersistence;

    private TransactionManager transactionManager;

    @Override
    @Tuning
    public ContextIDPersistence getContextIDPersistence() {
        return this.contextIDPersistence;
    }

    @Override
    @Tuning
    public ContextMapPersistence getContextMapPersistence() {
        return this.contextMapPersistence;
    }



    @Override
    @Tuning
    public ContextMetricsPersistence getContextMetricsPersistence() {
        return this.contextMetricsPersistence;
    }

    @Override
    @Tuning
    public ContextIDListenerPersistence getContextIDListenerPersistence() {
        return this.contextIDListenerPersistence;
    }

    @Override
    @Tuning
    public ContextKeyListenerPersistence getContextKeyListenerPersistence() {
        return this.contextKeyListenerPersistence;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public void setContextIDPersistence(ContextIDPersistence contextIDPersistence) {
        this.contextIDPersistence = contextIDPersistence;
    }

    public void setContextMapPersistence(ContextMapPersistence contextMapPersistence) {
        this.contextMapPersistence = contextMapPersistence;
    }


    public void setContextMetricsPersistence(ContextMetricsPersistence contextMetricsPersistence) {
        this.contextMetricsPersistence = contextMetricsPersistence;
    }

    public void setContextIDListenerPersistence(ContextIDListenerPersistence contextIDListenerPersistence) {
        this.contextIDListenerPersistence = contextIDListenerPersistence;
    }

    public void setContextKeyListenerPersistence(ContextKeyListenerPersistence contextKeyListenerPersistence) {
        this.contextKeyListenerPersistence = contextKeyListenerPersistence;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }


}
