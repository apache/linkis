package com.webank.wedatasphere.linkis.engineconnplugin.datax.client.config;

import com.webank.wedatasphere.linkis.engineconn.common.creation.EngineCreationContext;

public class ExecutionContext {
    private final EngineCreationContext environmentContext;

    public ExecutionContext(EngineCreationContext environmentContext) {
        this.environmentContext = environmentContext;
    }
}
