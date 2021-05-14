package com.webank.wedatasphere.linkis.filesystem.entity;

import com.webank.wedatasphere.linkis.filesystem.util.WorkspaceUtil;

public class LogLevel {

    private Type type;

    public LogLevel(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        ERROR(WorkspaceUtil.errorReg), WARN(WorkspaceUtil.warnReg), INFO(WorkspaceUtil.infoReg), ALL(WorkspaceUtil.allReg);
        private String reg;

        Type(String reg) {
            this.reg = reg;
        }

        public String getReg() {
            return this.reg;
        }
    }
}
