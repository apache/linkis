package com.webank.wedatasphere.linkis.cs.persistence;

import com.webank.wedatasphere.linkis.cs.persistence.persistence.*;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextPersistenceManager {

    ContextIDPersistence getContextIDPersistence();

    ContextMapPersistence getContextMapPersistence();



    ContextMetricsPersistence getContextMetricsPersistence();

    ContextIDListenerPersistence getContextIDListenerPersistence();

    ContextKeyListenerPersistence getContextKeyListenerPersistence();

    TransactionManager getTransactionManager();

}
