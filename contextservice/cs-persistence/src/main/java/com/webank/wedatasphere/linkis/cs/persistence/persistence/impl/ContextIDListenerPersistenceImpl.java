package com.webank.wedatasphere.linkis.cs.persistence.persistence.impl;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.CommonContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.persistence.dao.ContextIDListenerMapper;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextIDListener;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextIDListenerPersistence;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextIDPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by patinousward on 2020/2/17.
 */
@Component
public class ContextIDListenerPersistenceImpl implements ContextIDListenerPersistence {

    @Autowired
    private ContextIDListenerMapper contextIDListenerMapper;

    @Autowired
    private ContextIDPersistence contextIDPersistence;

    @Override
    public void create(ContextID contextID, ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        PersistenceContextIDListener listener = new PersistenceContextIDListener();
        listener.setContextId(contextID.getContextId());
        listener.setSource(contextIDListenerDomain.getSource());
        contextIDListenerMapper.createIDListener(listener);
    }

    @Override
    public void remove(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        // TODO: 2020/2/17
        PersistenceContextIDListener listener = new PersistenceContextIDListener();
        listener.setContextId(contextIDListenerDomain.getContextID().getContextId());
        listener.setSource(contextIDListenerDomain.getSource());
        contextIDListenerMapper.remove(listener);
    }

    @Override
    public void removeAll(ContextID contextID) throws CSErrorException {
        contextIDListenerMapper.removeAll(contextID);
    }

    @Override
    public List<ContextIDListenerDomain> getAll(ContextID contextID) throws CSErrorException {
        // 根据id返回一堆的domain
        ContextID complete = contextIDPersistence.getContextID(contextID.getContextId());
        List<PersistenceContextIDListener> listeners = contextIDListenerMapper.getAll(contextID);
        List<ContextIDListenerDomain> domains = listeners.stream().map(l -> pDomainToCommon(l, complete)).collect(Collectors.toList());
        return domains;
    }

    public ContextIDListenerDomain pDomainToCommon(PersistenceContextIDListener listener, ContextID contextID) {
        CommonContextIDListenerDomain domain = new CommonContextIDListenerDomain();
        domain.setContextID(contextID);
        domain.setSource(listener.getSource());
        return domain;
    }

    @Override
    public ContextIDListenerDomain getBy(ContextIDListenerDomain contextIDListenerDomain) throws CSErrorException {
        //根据id 和source 返回响应的ContextIDListenerDomain
        return contextIDListenerDomain;
    }
}
