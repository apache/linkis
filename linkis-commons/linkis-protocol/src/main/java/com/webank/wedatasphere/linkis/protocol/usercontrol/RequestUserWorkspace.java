package com.webank.wedatasphere.linkis.protocol.usercontrol;


public class RequestUserWorkspace implements UserControlLoginProtocol{

    private String userName;

    public RequestUserWorkspace(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


}
