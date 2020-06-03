package com.webank.wedatasphere.linkis.cs.contextcache;

import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextScope;
import com.webank.wedatasphere.linkis.cs.common.entity.enumeration.ContextType;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;

import java.util.List;

/**
 * @author peacewong
 * @date 2020/2/9 16:20
 */
public interface ContextCacheService {

   ContextKeyValue put(ContextID contextID, ContextKeyValue csKeyValue) throws CSErrorException;

   ContextKeyValue rest(ContextID contextID, ContextKey csKey);

   ContextKeyValue get(ContextID contextID, ContextKey csKey);

   List<ContextKeyValue> getValues(ContextID contextID, String keyword, ContextType csType);

   List<ContextKeyValue> getAllLikes(ContextID contextID, String regex, ContextType csType);

   List<ContextKeyValue> getAll(ContextID contextID);

   List<ContextKeyValue> getAllByScope(ContextID contextID, ContextScope scope, ContextType csType);

   List<ContextKeyValue> getAllByType(ContextID contextID, ContextType csType);

   ContextKeyValue remove(ContextID contextID, ContextKey csKey);

   void  removeAll(ContextID contextID);

   void  removeAll(ContextID contextID, ContextScope scope, ContextType csType);

   void  removeAll(ContextID contextID, ContextType csType);

   void removeByKeyPrefix(ContextID contextID, String preFix);

   void removeByKeyPrefix(ContextID contextID, String preFix, ContextType csType);
}
