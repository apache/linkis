package com.webank.wedatasphere.linkis.cs.persistence.dao;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextIDListener;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by patinousward on 2020/2/17.
 */
public interface ContextIDListenerMapper {

    void createIDListener(@Param("listener") PersistenceContextIDListener listener);

    void remove(@Param("listener") PersistenceContextIDListener listener);

    void removeAll(@Param("contextID") ContextID contextID);

    List<PersistenceContextIDListener> getAll(@Param("contextID") ContextID contextID);
}
