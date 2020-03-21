/**
 * @Title ThriftTransport.java
 * @description TODO
 * @time Nov 5, 2019 3:50:05 PM
 * @author dingqihuang
 * @version 1.0
**/
package com.webank.wedatasphere.linkis.engine.impala.client.thrift;

import java.io.IOException;
import java.net.Socket;
import java.security.SecureRandom;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.transport.TSaslClientTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import com.webank.wedatasphere.linkis.engine.impala.client.ImpalaTransport;
import com.webank.wedatasphere.linkis.engine.impala.client.util.Constant;

/**
 * @author dingqihuang
 * @version Nov 5, 2019
 */
public class ImpalaThriftTransport extends ImpalaTransport<TTransport> {

    private Socket socket = null;
    private TTransport tTransport = null;

    @Override
    public TTransport getTransport() throws Exception {
        if (tTransport != null && tTransport.isOpen()) {
            return tTransport;
        }

        SocketFactory socketFactory = null;

        /*
         * 是否加密传输
         */
        if (useSsl) {
            SSLContext context = SSLContext.getInstance(sslType);
            context.init(null, trustManagers, new SecureRandom());
            socketFactory = context.getSocketFactory();
        } else {
            socketFactory = SocketFactory.getDefault();
        }

        socket = socketFactory.createSocket(host, port);

        /*
         * 创建Thrift socket
         */
        TSocket tSocket = new TSocket(socket);
        tSocket.setConnectTimeout(connectionTimeout * 1000);

        /*
         * 使用动态密码，强制使用系统用户
         */
        if (useTicket) {
            username = System.getProperty("user.name");

            Process process = new ProcessBuilder().command(ticketBin).start();
            int ret = process.waitFor();
            if (ret != 0) {
                throw new RuntimeException(IOUtils.toString(process.getErrorStream(), Constant.DEFAULT_CHARSET));
            }
            password = IOUtils.toString(process.getInputStream(), Constant.DEFAULT_CHARSET);

        } else if (StringUtils.isBlank(username)) {
            /*
             * 未设置用户，使用系统用户
             */
            username = System.getProperty("user.name");
        }

        if (StringUtils.isNotBlank(password)) {
            CallbackHandler callbackHandler = new CallbackHandler() {
                @Override
                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
                    for (Callback callback : callbacks) {
                        if (callback instanceof NameCallback) {
                            ((NameCallback) callback).setName(username);
                            ;
                        } else if (callback instanceof PasswordCallback) {
                            ((PasswordCallback) callback).setPassword(password.toCharArray());
                        }
                    }
                }
            };
            /*
             * SASL+LDAP认证
             */
            tTransport = new TSaslClientTransport("PLAIN", (String) null, "LDAP", host, null, callbackHandler, tSocket);

        } else {
            /*
             * 普通连接
             */
            tTransport = tSocket;
        }

        if (!tTransport.isOpen()) {
            tTransport.open();
        }

        return tTransport;
    }

    @Override
    public void close() throws IOException {
        if (tTransport != null) {
            tTransport.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}
