package org.apache.linkis.engineconnplugin.datax.client.config;

import org.apache.linkis.engineconn.common.creation.EngineCreationContext;

public class ExecutionContext {
    private final EngineCreationContext environmentContext;

    public ExecutionContext(EngineCreationContext environmentContext) {
        this.environmentContext = environmentContext;
    }
}
