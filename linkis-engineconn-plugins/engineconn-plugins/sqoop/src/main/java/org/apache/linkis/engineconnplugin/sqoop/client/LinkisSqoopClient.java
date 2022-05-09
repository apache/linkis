/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.sqoop.client;

import org.apache.linkis.engineconnplugin.sqoop.client.utils.JarLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sqoop.SqoopOptions;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class LinkisSqoopClient {
    private static Class sqoopEngineClass;
    private static Logger logger = LoggerFactory.getLogger(LinkisSqoopClient.class);
    private static JarLoader jarLoader = null;
    public static int run(Map<String,String> params) {
        JarLoader jarLoader;
        try {
            jarLoader = new JarLoader(new String[]{
                    LinkisSqoopClient.class.getProtectionDomain().getCodeSource().getLocation().getPath()
            });
            //Load the sqoop class redefined by progress, notice that is not be resolved
            jarLoader.loadClass("org.apache.sqoop.mapreduce.JobBase", false);
            //Add the sqoop-{version}.jar to class path
            jarLoader.addJarURL(SqoopOptions.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            //Update the context loader
            Thread.currentThread().setContextClassLoader(jarLoader);
            sqoopEngineClass = jarLoader.loadClass("org.apache.linkis.engineconnplugin.sqoop.client.Sqoop");
            Method method = sqoopEngineClass.getDeclaredMethod("main",java.util.Map.class);
            return (Integer) method.invoke(null, params);
        } catch (Throwable e) {
            Writer result = new StringWriter();
            PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            e.printStackTrace();
            logger.error("Run Error Message:"+result.toString());
            return -1;
        }
    }
    public static void close(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(sqoopEngineClass !=null) {
                Method method = sqoopEngineClass.getDeclaredMethod("close");
                method.invoke(null);
            }
        } catch (Throwable e) {
            logger.error("Close Error Message:"+getLog(e));
        }

    }

    public static String getApplicationId(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("getApplicationId");
                return (String) method.invoke(null);
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient getApplicationId:"+getLog(e));
        }
        return "";
    }

    public static String getApplicationURL(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("getApplicationURL");
                return (String) method.invoke(null);
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient getApplicationURL:"+getLog(e));
        }
        return "";
    }

    public static float progress(){
        try {
            Thread.currentThread().setContextClassLoader(jarLoader);
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("progress");
                Float ret =  (Float) method.invoke(null);
                logger.info("LinkisSqoopClient progress ret:"+ret);
                return ret;
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient progress:"+getLog(e));
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
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("getProgressInfo");
                infoMap = (Map<String,Integer>) method.invoke(null);
                logger.error("LinkisSqoopClient getProgressInfo ret:"+infoMap.toString());
                return infoMap;
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient getProgressInfo:"+getLog(e));
        }
        return infoMap;
    }

    public static Map<String,Map<String,Long>> getMetrics(){
        Map<String,Map<String,Long>> metrics = new HashMap<>();
        Thread.currentThread().setContextClassLoader(jarLoader);
        try {
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("getMetrics");
                metrics = (Map<String,Map<String,Long>>) method.invoke(null);
                logger.error("LinkisSqoopClient getMetrics ret:"+metrics.toString());
                return metrics;
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient getMetrics:"+getLog(e));
        }
        return metrics;
    }
    public static Map<String,String[]> getDiagnosis(){
        Map<String,String[]> diagnosis = new HashMap<>();
        Thread.currentThread().setContextClassLoader(jarLoader);
        try {
            if(sqoopEngineClass != null){
                Method method = sqoopEngineClass.getDeclaredMethod("getDiagnosis");
                diagnosis = (Map<String,String[]>) method.invoke(null);
                logger.error("LinkisSqoopClient getDiagnosis ret:"+diagnosis.toString());
                return diagnosis;
            }
        }catch (Throwable e){
            logger.error("LinkisSqoopClient getDiagnosis:"+getLog(e));
        }
        return diagnosis;
    }

    private static String getLog(Throwable e){
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        return e.toString();
    }

}

