package org.apache.linkis.engineconnplugin.sqoop.client;

import org.apache.linkis.engineconnplugin.sqoop.client.utils.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.Map;

public class LinkisSqoopClient {
    private static Class sqoopEngineClass;
    private static Logger logger = LoggerFactory.getLogger(LinkisSqoopClient.class);
    public static int run(Map<String,String> params,String appHome) {
        JarLoader jarLoader = null;
        try {
            jarLoader = new JarLoader(new String[]{LinkisSqoopClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()});
            sqoopEngineClass=jarLoader.loadClass("org.apache.linkis.engineconnplugin.sqoop.client.Sqoop");
            Thread.currentThread().setContextClassLoader(jarLoader);
            jarLoader.addURL(appHome+"/lib2");
            Method method = sqoopEngineClass.getDeclaredMethod("main",java.util.Map.class);
            return (Integer) method.invoke(null,(Object) params);
        } catch (Throwable e) {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            logger.error("Run Error Message:"+result.toString());
            return -1;
        }
    }
    public static void close(){
        try {
            if(sqoopEngineClass !=null) {
                Method method = sqoopEngineClass.getDeclaredMethod("close");
                method.invoke(null);
            }
        } catch (Throwable e) {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            logger.error("Close Error Message:"+result.toString());
        }

    }
}

