package com.webank.wedatasphere.linkis.manager.common.protocol.conf;

import com.webank.wedatasphere.linkis.manager.label.entity.engine.EngineTypeLabel;
import com.webank.wedatasphere.linkis.manager.label.entity.engine.UserCreatorLabel;
import com.webank.wedatasphere.linkis.protocol.message.RequestProtocol;

/**
 * @author leebai
 * @date 2021/6/15 21:03
 */
public class RemoveCacheConfRequest implements RequestProtocol {

    private UserCreatorLabel userCreatorLabel;

    private EngineTypeLabel engineTypeLabel;

    public UserCreatorLabel getUserCreatorLabel() {
        return userCreatorLabel;
    }

    public void setUserCreatorLabel(UserCreatorLabel userCreatorLabel) {
        this.userCreatorLabel = userCreatorLabel;
    }

    public EngineTypeLabel getEngineTypeLabel() {
        return engineTypeLabel;
    }

    public void setEngineTypeLabel(EngineTypeLabel engineTypeLabel) {
        this.engineTypeLabel = engineTypeLabel;
    }
}
