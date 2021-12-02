package com.alibaba.datax.plugin.reader.ftpreader;


import java.util.function.Consumer;

/**
 * @author davidhua
 * 2019/7/4
 */
public class FtpConnParams {

    private String protocol;

    private String host;

    private int port;

    private int timeout;

    private String username;

    private String prvKeyPath;

    private String password;

    private String connectPattern;

    private FtpConnParams(){

    }

    public static FtpConnParams compose(Consumer<FtpConnParams> function){
        FtpConnParams ftpConnParams = new FtpConnParams();
        function.accept(ftpConnParams);
        return ftpConnParams;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPrvKeyPath() {
        return prvKeyPath;
    }

    public void setPrvKeyPath(String prvKeyPath) {
        this.prvKeyPath = prvKeyPath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConnectPattern() {
        return connectPattern;
    }

    public void setConnectPattern(String connectPattern) {
        this.connectPattern = connectPattern;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
