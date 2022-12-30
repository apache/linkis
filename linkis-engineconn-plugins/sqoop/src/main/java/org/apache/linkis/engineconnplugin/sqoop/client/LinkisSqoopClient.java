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

package org.apache.linkis.engineconnplugin.sqoop.client;

import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.engineconn.computation.executor.utlis.JarLoader;
import org.apache.linkis.protocol.engine.JobProgressInfo;

import org.apache.sqoop.SqoopOptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisSqoopClient {
  private static Class<?> sqoopEngineClass;
  private static Logger logger = LoggerFactory.getLogger(LinkisSqoopClient.class);
  private static JarLoader jarLoader;

  public static int run(Map<String, String> params) {
    try {
      jarLoader =
          new JarLoader(
              new String[] {
                LinkisSqoopClient.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath()
              });
      // Load the sqoop class redefined by progress, notice that is not be resolved
      jarLoader.loadClass("org.apache.sqoop.mapreduce.JobBase", false);
      // Add the sqoop-{version}.jar to class path
      jarLoader.addJarURL(
          SqoopOptions.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      // Update the context loader
      Thread.currentThread().setContextClassLoader(jarLoader);
      sqoopEngineClass =
          jarLoader.loadClass("org.apache.linkis.engineconnplugin.sqoop.client.Sqoop");
      Method method = sqoopEngineClass.getDeclaredMethod("main", Map.class);
      return (Integer) method.invoke(null, params);
    } catch (Throwable e) {
      logger.error("Run Error Message:" + getLog(e), e);
      return -1;
    }
  }

  /** Close */
  public static void close() {
    operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("close");
          method.invoke(null);
          return null;
        },
        e -> logger.error("Close Error Message: {}", getLog(e)));
  }

  /**
   * Fetch application id
   *
   * @return application id
   */
  public static String getApplicationId() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("getApplicationId");
          return (String) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient getApplicationId: {}", getLog(e)));
  }

  /**
   * Fetch application url
   *
   * @return url
   */
  public static String getApplicationURL() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("getApplicationURL");
          return (String) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient getApplicationURL: {}", getLog(e)));
  }

  /**
   * Progress value
   *
   * @return progress
   */
  public static Float progress() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("progress");
          return (Float) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient progress: {}", getLog(e)));
  }

  /**
   * ProgressInfo
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public static JobProgressInfo getProgressInfo() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("getProgressInfo");
          return (JobProgressInfo) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient getProgressInfo: {}", getLog(e)));
  }

  /**
   * Get metrics
   *
   * @return map value
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> getMetrics() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("getMetrics");
          return (Map<String, Object>) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient getMetrics: {}", getLog(e)));
  }

  /**
   * Get diagnosis
   *
   * @return map value
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> getDiagnosis() {
    return operateInClassLoader(
        jarLoader,
        () -> {
          Method method = sqoopEngineClass.getDeclaredMethod("getDiagnosis");
          return (Map<String, Object>) method.invoke(null);
        },
        e -> logger.error("Linkis SqoopClient getDiagnosis: {}", getLog(e)));
  }

  /**
   * Console log
   *
   * @param e throwable
   * @return log
   */
  private static String getLog(Throwable e) {
    Writer result = new StringWriter();
    PrintWriter printWriter = new PrintWriter(result);
    e.printStackTrace(printWriter);
    return e.toString();
  }

  /**
   * Operate in special classloader
   *
   * @param classLoader classloader
   * @param operation operation
   * @param resolver resolver
   * @param <R> return type
   * @return return
   */
  private static <R> R operateInClassLoader(
      ClassLoader classLoader, ClientOperation<R> operation, Consumer<Throwable> resolver) {
    ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
    R result = null;
    try {
      Thread.currentThread().setContextClassLoader(classLoader);
      result = operation.operate();
    } catch (Exception t) {
      resolver.accept(t);
    } finally {
      Thread.currentThread().setContextClassLoader(currentLoader);
    }
    return result;
  }

  @FunctionalInterface
  interface ClientOperation<T> {

    /**
     * Operate
     *
     * @return T
     * @throws ErrorException error exception
     */
    T operate()
        throws ErrorException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException;
  }
}
