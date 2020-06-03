package com.webank.wedatasphere.linkis.cs.persistence.dao;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.persistence.entity.PersistenceContextKeyListener;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by patinousward on 2020/2/17.
 */
public interface ContextKeyListenerMapper {

    void createKeyListener(@Param("listener") PersistenceContextKeyListener listener);

    void remove(@Param("listener") ContextKeyListenerDomain contextKeyListenerDomain, @Param("keyId") Integer keyId);

    void removeAll(@Param("keyIds") List<Integer> keyIds);

    List<PersistenceContextKeyListener> getAll(@Param("keyIds") List<Integer> keyIds);
}
