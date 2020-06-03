package com.webank.wedatasphere.linkis.cs.server.service;

import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextIDListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.listener.ContextKeyListenerDomain;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextID;
import com.webank.wedatasphere.linkis.cs.common.entity.source.ContextKey;
import com.webank.wedatasphere.linkis.cs.common.exception.CSErrorException;
import com.webank.wedatasphere.linkis.cs.listener.callback.imp.ContextKeyValueBean;

import java.util.List;

/**
 * Created by patinousward on 2020/2/18.
 */
public abstract class ContextListenerService extends AbstractService {

    public abstract void onBind(ContextID contextID, ContextIDListenerDomain listener) throws CSErrorException;

    public abstract void onBind(ContextID contextID, ContextKey contextKey, ContextKeyListenerDomain listener) throws CSErrorException;

    public abstract List<ContextKeyValueBean> heartbeat(String source);

}
