package com.webank.wedatasphere.linkis.manager.common.protocol.engine;

import com.webank.wedatasphere.linkis.manager.common.entity.node.EngineNode;

/**
 * @author peacewong
 * @date 2020/8/4 21:32
 */
public class EngineInfoClearRequest implements EngineRequest {


    private EngineNode engineNode;

    private String user;

    public EngineInfoClearRequest() {

    }

    public EngineNode getEngineNode() {
        return engineNode;
    }

    public void setEngineNode(EngineNode engineNode) {
        this.engineNode = engineNode;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public String getUser() {
        return user;
    }
}
