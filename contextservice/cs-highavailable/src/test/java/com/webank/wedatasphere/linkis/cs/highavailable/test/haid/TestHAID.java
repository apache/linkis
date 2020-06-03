package com.webank.wedatasphere.linkis.cs.highavailable.test.haid;

import com.webank.wedatasphere.linkis.cs.common.entity.source.HAContextID;

public class TestHAID implements HAContextID {

    private String contextId;
    private String instance;
    private String backupInstance;

    @Override
    public String getContextId() {
        return contextId;
    }

    @Override
    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    @Override
    public String getInstance() {
        return instance;
    }

    @Override
    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String getBackupInstance() {
        return backupInstance;
    }

    @Override
    public void setBackupInstance(String backupInstance) {
        this.backupInstance = backupInstance;
    }
}
