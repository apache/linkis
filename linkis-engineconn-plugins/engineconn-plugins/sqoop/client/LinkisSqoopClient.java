/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client;

import com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.utils.JarLoader;
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
            sqoopEngineClass=jarLoader.loadClass("com.webank.wedatasphere.linkis.engineconnplugin.sqoop.client.Sqoop");
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
