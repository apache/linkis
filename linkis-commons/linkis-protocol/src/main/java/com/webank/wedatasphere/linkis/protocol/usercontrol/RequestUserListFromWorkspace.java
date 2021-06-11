package com.webank.wedatasphere.linkis.protocol.usercontrol;

import java.util.List;


public class RequestUserListFromWorkspace  implements UserControlLoginProtocol {
    public RequestUserListFromWorkspace(List<Integer> userWorkspaceIds) {
        this.userWorkspaceIds = userWorkspaceIds;
    }

    public List<Integer> getUserWorkspaceIds() {
        return userWorkspaceIds;
    }

    public void setUserWorkspaceIds(List<Integer> userWorkspaceIds) {
        this.userWorkspaceIds = userWorkspaceIds;
    }

    private List<Integer> userWorkspaceIds;
}
