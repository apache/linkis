package com.webank.wedatasphere.linkis.protocol.usercontrol;

import java.util.List;


public class ResponseWorkspaceUserList implements UserControlLoginProtocol {
    public ResponseWorkspaceUserList(List<String> userNames) {
        this.userNames = userNames;
    }

    public List<String> getUserNames() {
        return userNames;
    }

    public void setUserNames(List<String> userNames) {
        this.userNames = userNames;
    }

    private List<String> userNames;
}
