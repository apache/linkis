package org.apache.linkis.engineconnplugin.datax.client;

import com.alibaba.datax.common.util.Configuration;
import org.apache.linkis.engineconnplugin.datax.client.utils.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkisDataxClient {
    private static Logger logger = LoggerFactory.getLogger(LinkisDataxClient.class);
    private static Class<?> dataxEngineClass;
    private static JarLoader jarLoader;
    public static int main(String[] args) {
       
        try {
            jarLoader = new JarLoader(new String[]{
                    LinkisDataxClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()
            });
            //Load the datax class redefined by progress, notice that is not be resolved
            jarLoader.loadClass("com.alibaba.datax.core.AbstractContainer", false);
            jarLoader.loadClass("com.alibaba.datax.core.job.JobContainer", false);
            jarLoader.loadClass("com.alibaba.datax.core.taskgroup.TaskGroupContainer",false);
            dataxEngineClass = jarLoader.loadClass("com.alibaba.datax.core.Engine");
            //Add the datax-core-{version}.jar to class path
            jarLoader.addJarURL(Configuration.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            //Update the context loader
            Thread.currentThread().setContextClassLoader(jarLoader);
            Method method = dataxEngineClass.getDeclaredMethod("main",String[].class);
            return (Integer) method.invoke(null, (Object) args);
        } catch (Throwable e) {
            logger.error("Run Error Message:"+getLog(e));
            return -1;
        }
    }
    private static String getLog(Throwable e){
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return e.toString();
    }

    public static void close(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(dataxEngineClass !=null) {
                Method method = dataxEngineClass.getDeclaredMethod("close");
                method.invoke(null);
            }
        } catch (Throwable e) {
            logger.error("Close Error Message:"+getLog(e));
        }

    }

    public static float progress(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(dataxEngineClass != null){
                Method method = dataxEngineClass.getDeclaredMethod("progress");
                Float ret =  (Float) method.invoke(null);
                logger.info("LinkisDataxClient progress ret:"+ret);
                return ret;
            }
        }catch (Throwable e){
            logger.error("LinkisDataxClient progress:"+getLog(e));
        }
        return 0.0f;
    }


    public static Map<String,Integer> getProgressInfo(){
        Map<String, Integer> infoMap = new HashMap();
        infoMap.put("totalTasks", 0);
        infoMap.put("runningTasks", 0);
        infoMap.put("failedTasks", 0);
        infoMap.put("succeedTasks", 0);
        Thread.currentThread().setContextClassLoader(jarLoader);
        try {
            if(dataxEngineClass != null){
                Method method = dataxEngineClass.getDeclaredMethod("getProgressInfo");
                infoMap = (Map<String,Integer>) method.invoke(null);
                logger.error("LinkisDataxClient getProgressInfo ret:"+infoMap.toString());
                return infoMap;
            }
        }catch (Throwable e){
            logger.error("LinkisDataxClient getProgressInfo:"+getLog(e));
        }
        return infoMap;
    }

    public static Map<String,Long> getMetrics(){
        Map<String,Long> metrics = new HashMap<>();
        Thread.currentThread().setContextClassLoader(jarLoader);
        try {
            if(dataxEngineClass != null){
                Method method = dataxEngineClass.getDeclaredMethod("getMetrics");
                metrics = (Map<String,Long>) method.invoke(null);
                logger.error("LinkisDataxClient getMetrics ret:"+metrics.toString());
                return metrics;
            }
        }catch (Throwable e){
            logger.error("LinkisDataxClient getMetrics:"+getLog(e));
        }
        return metrics;
    }
    public static Map<String, List<String>> getDiagnosis(){
        Map<String,List<String>> diagnosis = new HashMap<>();
        Thread.currentThread().setContextClassLoader(jarLoader);
        try {
            if(dataxEngineClass != null){
                Method method = dataxEngineClass.getDeclaredMethod("getDiagnosis");
                diagnosis = (Map<String, List<String>>) method.invoke(null);
                return diagnosis;
            }
        }catch (Throwable e){
            logger.error("LinkisDataxClient getDiagnosis:"+getLog(e));
        }
        return diagnosis;
    }

    public static String getJobId(){
        try {
            if(dataxEngineClass != null){
                Method method = dataxEngineClass.getDeclaredMethod("getJobId");
                return  (String) method.invoke(null);
            }
        }catch (Throwable e){
            logger.error("LinkisDataxClient getJobId:"+getLog(e));
        }
        return "-1";
    }
}
