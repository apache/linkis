package com.webank.wedatasphere.linkis.cs.persistence.persistence.impl;

import com.webank.wedatasphere.linkis.cs.persistence.persistence.TransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Created by patinousward on 2020/2/17.
 */
@Component
public class TransactionManagerImpl implements TransactionManager {

    @Autowired
    private PlatformTransactionManager txManager;

    @Override
    public Object begin() {
        return txManager.getTransaction(new DefaultTransactionDefinition());
    }

    @Override
    public void rollback(Object object) {
        TransactionStatus status = (TransactionStatus) object;
        txManager.rollback(status);
    }

    @Override
    public void commit(Object object) {
        TransactionStatus status = (TransactionStatus) object;
        txManager.commit(status);
    }

    @Override
    public void onTransaction() {
        // TODO: 2020/2/17  
    }
}
