package com.webank.wedatasphere.linkis.cs.persistence.persistence;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

import java.util.List;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextIDListenerPersistence {

    void create(ContextID contextID, ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException;

    void remove(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException;

    void removeAll(ContextID contextID) throws CSErrorException;

    List<ContextIDListenerDomain> getAll(ContextID contextID) throws CSErrorException;

    ContextIDListenerDomain getBy(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException;


}
