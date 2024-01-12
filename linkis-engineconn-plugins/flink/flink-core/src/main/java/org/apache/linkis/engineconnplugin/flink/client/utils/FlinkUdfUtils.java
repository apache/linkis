/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.flink.client.utils;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.functions.UserDefinedFunction;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkUdfUtils {

  private static final Logger logger = LoggerFactory.getLogger(FlinkUdfUtils.class);

  private static final String CREATE_TEMP_FUNCTION_PATTERN =
      "create\\s+temporary\\s+function\\s+(\\w+)\\s+as\\s+\"(.*?)\"";

  private static final String CREATE_TEMP_FUNCTION_SQL =
      "CREATE TEMPORARY FUNCTION IF NOT EXISTS %s AS '%s' ";

  public static void addFlinkPipelineClasspaths(StreamExecutionEnvironment env, String path) {
    logger.info("Flink udf start add pipeline classpaths, jar path: {}", path);

    try {
      Field configuration = StreamExecutionEnvironment.class.getDeclaredField("configuration");
      configuration.setAccessible(true);
      Configuration conf = (Configuration) configuration.get(env);

      Field confData = Configuration.class.getDeclaredField("confData");
      confData.setAccessible(true);
      Map<String, Object> map = (Map<String, Object>) confData.get(conf);
      List<String> jarList = new ArrayList<>();
      List<String> oldList =
          conf.getOptional(PipelineOptions.CLASSPATHS).orElseGet(Collections::emptyList);
      if (CollectionUtils.isNotEmpty(oldList)) {
        jarList.addAll(oldList);
      }
      jarList.add(path);
      map.put(PipelineOptions.CLASSPATHS.key(), jarList);
    } catch (Exception e) {
      logger.warn("Flink udf add pipeline classpaths failed", e);
    }
  }

  public static void loadJar(String jarPath) {
    logger.info("Flink udf URLClassLoader start loadJar: {}", jarPath);

    Method method = null;
    Boolean accessible = null;
    try {
      method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
      accessible = method.isAccessible();

      if (accessible == false) {
        method.setAccessible(true);
      }
      URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
      method.invoke(classLoader, new URL(jarPath));

    } catch (Exception e) {
      logger.warn("Flink udf URLClassLoader loadJar failed", e);
    } finally {
      if (accessible != null) {
        method.setAccessible(accessible);
      }
    }
  }

  public static String extractUdfClass(String statement) {
    Pattern pattern = Pattern.compile(CREATE_TEMP_FUNCTION_PATTERN);
    Matcher matcher = pattern.matcher(statement);
    if (matcher.find() && matcher.groupCount() >= 2) {
      return matcher.group(2);
    }
    return "";
  }

  public static boolean isFlinkUdf(ClassLoader classLoader, String className) {
    try {
      Class<?> udfClass = classLoader.loadClass(className);
      if (UserDefinedFunction.class.isAssignableFrom(udfClass)) {
        return true;
      }

    } catch (ClassNotFoundException e) {
      logger.warn("flink udf load isFlinkUdf failed, ClassNotFoundException: {}", className);
    }
    return false;
  }

  public static String generateFlinkUdfSql(String name, String className) {
    return String.format(CREATE_TEMP_FUNCTION_SQL, name, className);
  }
}
