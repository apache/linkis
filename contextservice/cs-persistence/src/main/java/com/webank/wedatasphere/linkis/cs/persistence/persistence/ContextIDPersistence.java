package com.webank.wedatasphere.linkis.cs.persistence.persistence;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.listener.ContextIDListener;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextIDPersistence {

    ContextID createContextID(ContextID contextID) throws CSErrorException;

    void deleteContextID(String contextId) throws CSErrorException;

    void updateContextID(ContextID contextID) throws CSErrorException;

    ContextID getContextID(String contextId) throws CSErrorException;

}
