package com.webank.wedatasphere.linkis.protocol.usercontrol;

import java.util.List;


public class ResponseUserWorkspace implements UserControlLoginProtocol{

    List<Integer> userWorkspaceIds;

    public ResponseUserWorkspace(List<Integer> userWorkspaceIds) {
        this.userWorkspaceIds = userWorkspaceIds;
    }

    public List<Integer> getUserWorkspaceIds() {
        return userWorkspaceIds;
    }

    public void setUserWorkspaceIds(List<Integer> userWorkspaceIds) {
        this.userWorkspaceIds = userWorkspaceIds;
    }


}
