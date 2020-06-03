package com.webank.wedatasphere.linkis.cs.persistence.persistence;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.common.listener.ContextKeyListener;

import java.util.List;

/**
 * Created by patinousward on 2020/2/11.
 */
public interface ContextMapPersistence {

    void create(ContextID contextID, ContextKeyValue contextKeyValue) throws CSErrorException;

    void update(ContextID contextID, ContextKeyValue contextKeyValue) throws CSErrorException;

    ContextKeyValue get(ContextID contextID, ContextKey contextKey) throws CSErrorException;

    List<ContextKeyValue> getAll(ContextID contextID, String key) throws CSErrorException;

    List<ContextKeyValue> getAll(ContextID contextID) throws CSErrorException;

    List<ContextKeyValue> getAll(ContextID contextID, ContextScope contextScope) throws CSErrorException;

    List<ContextKeyValue> getAll(ContextID contextID, ContextType contextType) throws CSErrorException;

    void reset(ContextID contextID, ContextKey contextKey) throws CSErrorException;

    void remove(ContextID contextID, ContextKey contextKey) throws CSErrorException;

    void removeAll(ContextID contextID) throws CSErrorException;

    void removeAll(ContextID contextID, ContextType contextType) throws CSErrorException;

    void removeAll(ContextID contextID, ContextScope contextScope) throws CSErrorException;

    void removeByKeyPrefix(ContextID contextID, String keyPrefix);

    void removeByKeyPrefix(ContextID contextID, ContextType contextType, String keyPrefix);
}
