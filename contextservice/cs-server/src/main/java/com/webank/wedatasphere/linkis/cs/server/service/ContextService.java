package com.webank.wedatasphere.linkis.cs.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.condition.Condition;
import com.webank.wedatasphere.linkis.cs.exception.ContextSearchFailedException;

import java.util.List;
import java.util.Map;

/**
 * Created by patinousward on 2020/2/18.
 */
public abstract class ContextService extends AbstractService {

    public abstract ContextValue getContextValue(ContextID contextID, ContextKey contextKey);

    public abstract List<ContextKeyValue> searchContextValue(ContextID contextID, Map<Object, Object> conditionMap) throws ContextSearchFailedException;

    //public abstract List<ContextKeyValue> searchContextValueByCondition(Condition condition) throws ContextSearchFailedException;

    public abstract void setValueByKey(ContextID contextID, ContextKey contextKey, ContextValue contextValue) throws CSErrorException, ClassNotFoundException, JsonProcessingException;

    public abstract void setValue(ContextID contextID, ContextKeyValue contextKeyValuee) throws CSErrorException, ClassNotFoundException, JsonProcessingException;

    public abstract void resetValue(ContextID contextID, ContextKey contextKey) throws CSErrorException;

    public abstract void removeValue(ContextID contextID, ContextKey contextKey) throws CSErrorException;

    public abstract void removeAllValue(ContextID contextID) throws CSErrorException;

    public abstract void removeAllValueByKeyPrefixAndContextType(ContextID contextID, ContextType contextType,String keyPrefix) throws CSErrorException;

    public abstract void removeAllValueByKeyPrefix(ContextID contextID,String keyPrefix) throws CSErrorException;

}
