package com.webank.wedatasphere.linkis.cs.execution.ruler;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;

import java.util.List;

public interface ContextSearchRuler {

    List<ContextKeyValue> rule(List<ContextKeyValue> contextKeyValues);

}
