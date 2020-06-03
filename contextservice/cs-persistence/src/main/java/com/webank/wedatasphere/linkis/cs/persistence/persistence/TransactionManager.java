package com.webank.wedatasphere.linkis.cs.persistence.persistence;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface TransactionManager {

    Object begin();

    void rollback(Object object);

    void commit(Object object);

    void onTransaction();

}
