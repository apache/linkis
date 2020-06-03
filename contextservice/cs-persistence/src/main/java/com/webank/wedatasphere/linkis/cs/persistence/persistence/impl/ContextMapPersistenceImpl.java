package com.webank.wedatasphere.linkis.cs.persistence.persistence.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.ContextSerializationHelper;
import com.webank.wedatasphere.linkis.cs.common.serialize.helper.SerializationHelper;
import com.webank.wedatasphere.linkis.cs.persistence.dao.ContextMapMapper;
import com.webank.wedatasphere.linkis.cs.persistence.entity.ExtraFieldClass;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKey;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKeyValue;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextValue;
import com.webank.wedatasphere.linkis.cs.persistence.persistence.ContextMapPersistence;
import com.webank.wedatasphere.linkis.cs.persistence.util.PersistenceUtils;
import com.webank.wedatasphere.linkis.server.BDPJettyServerHelper;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by patinousward on 2020/2/13.
 */
@Component
public class ContextMapPersistenceImpl implements ContextMapPersistence {

    @Autowired
    private ContextMapMapper contextMapMapper;

    private Class<PersistenceContextKey> pKClass = PersistenceContextKey.class;

    private Class<PersistenceContextValue> pVClass = PersistenceContextValue.class;

    private Class<PersistenceContextKeyValue> pKVClass = PersistenceContextKeyValue.class;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ObjectMapper json = BDPJettyServerHelper.jacksonJson();

    private final SerializationHelper serialHelper = ContextSerializationHelper.getInstance();

    @Override
    public void create(ContextID contextID, ContextKeyValue kV) throws CSErrorException {
        // TODO: 2020/2/17 keywords 如何合并
        try {
            Pair<PersistenceContextKey, ExtraFieldClass> pK = PersistenceUtils.transfer(kV.getContextKey(), pKClass);
            Pair<PersistenceContextValue, ExtraFieldClass> pV = PersistenceUtils.transfer(kV.getContextValue(), pVClass);
            Pair<PersistenceContextKeyValue, ExtraFieldClass> pKV = PersistenceUtils.transfer(kV, pKVClass);
            pV.getFirst().setValueStr(serialHelper.serialize(kV.getContextValue().getValue()));
            pKV.getSecond().addSub(pK.getSecond());
            pKV.getSecond().addSub(pV.getSecond());
            pKV.getFirst().setProps(json.writeValueAsString(pKV.getSecond()));
            pKV.getFirst().setContextId(contextID.getContextId());
            pKV.getFirst().setContextKey(pK.getFirst());
            pKV.getFirst().setContextValue(pV.getFirst());
            contextMapMapper.createMap(pKV.getFirst());
        } catch (JsonProcessingException e) {
            logger.error("writeAsJson failed:", e);
            throw new CSErrorException(97000, e.getMessage());
        }
    }

    @Override
    public void update(ContextID contextID, ContextKeyValue kV) throws CSErrorException {
        //根据contextId和key 进行更新
        Pair<PersistenceContextKey, ExtraFieldClass> pK = PersistenceUtils.transfer(kV.getContextKey(), pKClass);
        Pair<PersistenceContextValue, ExtraFieldClass> pV = PersistenceUtils.transfer(kV.getContextValue(), pVClass);
        Pair<PersistenceContextKeyValue, ExtraFieldClass> pKV = PersistenceUtils.transfer(kV, pKVClass);
        Object value = kV.getContextValue().getValue();
        if (value != null) {
            pV.getFirst().setValueStr(serialHelper.serialize(value));
        }
        pKV.getFirst().setContextKey(pK.getFirst());
        pKV.getFirst().setContextValue(pV.getFirst());
        pKV.getFirst().setContextId(contextID.getContextId());
        contextMapMapper.updateMap(pKV.getFirst());
    }

    @Override
    public ContextKeyValue get(ContextID contextID, ContextKey contextKey) throws CSErrorException {
        //根据contextId 和key,获取到一个ContextKeyValue
        PersistenceContextKeyValue pKV = contextMapMapper.getContextMap(contextID, contextKey);
        if (pKV == null) return null;
        return transfer(pKV);
    }

    private ContextKeyValue transfer(PersistenceContextKeyValue pKV) throws CSErrorException {
        try {
            // TODO: 2020/2/14 null return
            PersistenceContextKey pK = (PersistenceContextKey) pKV.getContextKey();
            PersistenceContextValue pV = (PersistenceContextValue) pKV.getContextValue();
            ExtraFieldClass extraFieldClass = json.readValue(pKV.getProps(), ExtraFieldClass.class);
            ContextKey key = PersistenceUtils.transfer(extraFieldClass.getOneSub(0), pK);
            ContextValue value = PersistenceUtils.transfer(extraFieldClass.getOneSub(1), pV);
            if(value != null){
                value.setValue(serialHelper.deserialize(pV.getValueStr()));
            }
            ContextKeyValue kv = PersistenceUtils.transfer(extraFieldClass, pKV);
            kv.setContextKey(key);
            kv.setContextValue(value);
            return kv;
        } catch (IOException e) {
            logger.error("readJson failed:", e);
            throw new CSErrorException(97000, e.getMessage());
        }
    }

    @Override
    public List<ContextKeyValue> getAll(ContextID contextID, String key) {
        //模糊匹配key
        List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByKey(contextID, key);
        return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
    }

    @Override
    public List<ContextKeyValue> getAll(ContextID contextID) {
        List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByContextID(contextID);
        return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
    }

    @Override
    public List<ContextKeyValue> getAll(ContextID contextID, ContextScope contextScope) {
        List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByScope(contextID, contextScope);
        return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
    }

    @Override
    public List<ContextKeyValue> getAll(ContextID contextID, ContextType contextType) {
        List<PersistenceContextKeyValue> pKVs = contextMapMapper.getAllContextMapByType(contextID, contextType);
        return pKVs.stream().map(PersistenceUtils.map(this::transfer)).collect(Collectors.toList());
    }

    @Override
    public void reset(ContextID contextID, ContextKey contextKey) {

    }

    @Override
    public void remove(ContextID contextID, ContextKey contextKey) {
        contextMapMapper.removeContextMap(contextID, contextKey);
    }

    @Override
    public void removeAll(ContextID contextID) {
        contextMapMapper.removeAllContextMapByContextID(contextID);
    }

    @Override
    public void removeAll(ContextID contextID, ContextType contextType) {
        contextMapMapper.removeAllContextMapByType(contextID, contextType);
    }

    @Override
    public void removeAll(ContextID contextID, ContextScope contextScope) {
        contextMapMapper.removeAllContextMapByScope(contextID, contextScope);
    }

    @Override
    public void removeByKeyPrefix(ContextID contextID, String keyPrefix) {
        contextMapMapper.removeByKeyPrefix(contextID,keyPrefix);
    }

    @Override
    public void removeByKeyPrefix(ContextID contextID, ContextType contextType, String keyPrefix) {
        contextMapMapper.removeByKeyPrefixAndContextType(contextID,contextType,keyPrefix);
    }

}
