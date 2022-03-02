package org.apache.linkis.proxy;

import org.apache.commons.lang.StringUtils;

public class ProxyUserEntity {

    private String username;

    private String proxyUser;

    private String desc;

    private Long elapseDay;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Long getElapseDay() {
        return elapseDay;
    }

    public void setElapseDay(Long elapseDay) {
        this.elapseDay = elapseDay;
    }

    public boolean isProxyMode() {
        return StringUtils.isNotBlank(proxyUser) && !proxyUser.equals(username);
    }

    @Override
    public String toString() {
        return "ProxyUserEntity{"
                + "username='"
                + username
                + '\''
                + ", proxyUser='"
                + proxyUser
                + '\''
                + '}';
    }
}
