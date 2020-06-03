package com.webank.wedatasphere.linkis.cs.persistence.dao;

import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextID;

/**
 * Created by patinousward on 2020/2/13.
 */
public interface ContextIDMapper {
    void createContextID(PersistenceContextID persistenceContextID);

    void deleteContextID(String contextId);

    PersistenceContextID getContextID(String contextId);

    void updateContextID(PersistenceContextID persistenceContextID);
}
