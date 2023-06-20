package org.apache.linkis.engineconnplugin.seatunnel.client;

import org.apache.linkis.engineconn.computation.executor.utlis.JarLoader;
import org.apache.seatunnel.core.starter.seatunnel.SeaTunnelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;

public class LinkSeatunnelZetaClient {
    private static Logger logger = LoggerFactory.getLogger(LinkSeatunnelZetaClient.class);
    private static Class<?> seatunnelEngineClass;
    private static JarLoader jarLoader;

    public static int main(String[] args) {
        try {
            jarLoader =
                    new JarLoader(
                            new String[] {
                                    LinkSeatunnelZetaClient.class
                                            .getProtectionDomain()
                                            .getCodeSource()
                                            .getLocation()
                                            .getPath()
                            });
            jarLoader.loadClass("org.apache.seatunnel.common.config.Common", false);
            jarLoader.loadClass("org.apache.seatunnel.core.base.config.ConfigBuilder", false);
            //      jarLoader.loadClass("org.apache.seatunnel.core.base.config.PluginFactory", false);
            seatunnelEngineClass = jarLoader.loadClass("org.apache.seatunnel.core.zeta.ZetaStarter");
            jarLoader.addJarURL(
                    SeaTunnelClient.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
                            .getPath());
            Thread.currentThread().setContextClassLoader(jarLoader);
            Method method = seatunnelEngineClass.getDeclaredMethod("main", String[].class);
            return (Integer) method.invoke(null, (Object) args);
        } catch (Throwable e) {
            logger.error("Run Error Message:" + getLog(e));
            return -1;
        }
    }

    private static String getLog(Throwable e) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return e.toString();
    }
}
