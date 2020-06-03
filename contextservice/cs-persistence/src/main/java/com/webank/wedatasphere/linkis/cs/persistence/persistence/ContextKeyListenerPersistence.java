package com.webank.wedatasphere.linkis.cs.persistence.persistence;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.listener.ContextKeyListener;

import java.util.List;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextKeyListenerPersistence {

    void create(ContextID contextID, ContextKeyListenerDomain contextKeyListenerDomain) throws CSErrorException;

    void remove(ContextID contextID, ContextKeyListenerDomain contextKeyListenerDomain) throws CSErrorException;

    void removeAll(ContextID contextID) throws CSErrorException;

    List<ContextKeyListenerDomain> getAll(ContextID contextID) throws CSErrorException;

    ContextKeyListenerDomain getBy(ContextKeyListenerDomain contextKeyListenerDomain) throws CSErrorException;


}
