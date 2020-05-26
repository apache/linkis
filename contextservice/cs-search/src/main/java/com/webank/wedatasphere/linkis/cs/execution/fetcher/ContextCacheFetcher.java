package com.webank.wedatasphere.linkis.cs.execution.fetcher;

import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKeyValue;

import java.util.List;

public interface ContextCacheFetcher {

    List<ContextKeyValue> fetch(ContextID contextID);

}
