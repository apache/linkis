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
