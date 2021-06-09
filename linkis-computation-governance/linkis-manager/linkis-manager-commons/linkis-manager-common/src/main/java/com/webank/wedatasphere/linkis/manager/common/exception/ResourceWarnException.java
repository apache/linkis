package com.webank.wedatasphere.linkis.manager.common.exception;

import com.webank.wedatasphere.linkis.common.exception.WarnException;

public class ResourceWarnException  extends WarnException {
    public ResourceWarnException(int errCode, String desc) {
        super(errCode, desc);
    }

    public ResourceWarnException(int errCode, String desc, String ip, int port, String serviceKind) {
        super(errCode, desc, ip, port, serviceKind);
    }
}
