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

package org.apache.linkis.engineplugin.loader.utils;

import org.apache.linkis.manager.engineplugin.common.EngineConnPlugin;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Description: Provide some methods to find classpath */
public class EngineConnPluginUtils {

  private static final Logger LOG = LoggerFactory.getLogger(EngineConnPluginUtils.class);

  private static final String JAR_SUF_NAME = ".jar";

  private static final String CLASS_SUF_NAME = ".class";

  public static final String FILE_SCHEMA = "file://";

  private static final Class<EngineConnPlugin> PLUGIN_PARENT_CLASS = EngineConnPlugin.class;

  public static List<URL> getJarsUrlsOfPath(String path) {
    List<URL> classPathURLs = new ArrayList<>();
    return getJarsUrlsOfPathRecurse(path, classPathURLs);
  }

  public static String getEngineConnPluginClass(URLClassLoader engineClassloader) {
    URL[] urlsOfClassLoader = engineClassloader.getURLs();
    String className;
    for (URL classPath : urlsOfClassLoader) {
      String path = classPath.getPath();
      className =
          getEngineConnPluginClassFromURL(
              path,
              (classFullName) -> isSubEngineConnPluginClass(classFullName, engineClassloader));
      if (null != className) {
        return className;
      }
    }
    return null;
  }

  /**
   * Load EnginePlugin in spi( you should have constructor with no parameters)
   *
   * @param classLoader engine plugin classloader
   * @return
   */
  public static EngineConnPlugin loadSubEngineConnPluginInSpi(ClassLoader classLoader) {
    ServiceLoader<EngineConnPlugin> serviceLoader =
        ServiceLoader.load(PLUGIN_PARENT_CLASS, classLoader);
    Iterator<EngineConnPlugin> subClassIterator = serviceLoader.iterator();
    // Just return the first one
    if (subClassIterator.hasNext()) {
      return subClassIterator.next();
    }
    return null;
  }

  private static List<URL> getJarsUrlsOfPathRecurse(String path, List<URL> classPathURLs) {
    File parentFile = new File(path);
    File[] childFiles =
        parentFile.listFiles(
            (file) -> {
              String name = file.getName();
              return !name.startsWith(".")
                  && (file.isDirectory()
                      || name.endsWith(JAR_SUF_NAME)
                      || name.endsWith(CLASS_SUF_NAME));
            });
    if (null != childFiles && childFiles.length > 0) {
      for (File childFile : childFiles) {
        if (childFile.isDirectory()) {
          getJarsUrlsOfPathRecurse(childFile.getPath(), classPathURLs);
        } else {
          try {
            classPathURLs.add(childFile.toURI().toURL());
          } catch (MalformedURLException e) {
            // Ignore
            LOG.warn("url {} cannot be added", FILE_SCHEMA + childFile.getPath());
          }
        }
      }
    }
    return classPathURLs;
  }

  /**
   * Get engine plugin class
   *
   * @param url url
   * @param acceptedFunction accept function
   * @return
   */
  private static String getEngineConnPluginClassFromURL(
      String url, Function<String, Boolean> acceptedFunction) {
    if (url.endsWith(CLASS_SUF_NAME)) {
      String className = url.substring(0, url.lastIndexOf(CLASS_SUF_NAME));
      int splitIndex = className.lastIndexOf(IOUtils.DIR_SEPARATOR);
      if (splitIndex >= 0) {
        className = className.substring(splitIndex);
      }
      return acceptedFunction.apply(className) ? className : null;
    } else if (url.endsWith(JAR_SUF_NAME)) {
      try {
        JarFile jarFile = new JarFile(new File(url));
        Enumeration<JarEntry> en = jarFile.entries();
        while (en.hasMoreElements()) {
          String name = en.nextElement().getName();
          if (name.endsWith(CLASS_SUF_NAME)) {
            String className = name.substring(0, name.lastIndexOf(CLASS_SUF_NAME));
            // If the splicer is different in WINDOWS system?
            className = className.replaceAll(String.valueOf(IOUtils.DIR_SEPARATOR_UNIX), ".");
            if (acceptedFunction.apply(className)) {
              return className;
            }
          }
        }
        return null;
      } catch (IOException e) {
        // Trace
        LOG.trace("Fail to parse jar file:[" + url + "] in plugin classpath");
        return null;
      }
    }
    return null;
  }

  private static boolean isSubEngineConnPluginClass(String className, ClassLoader classLoader) {
    if (StringUtils.isEmpty(className)) {
      return false;
    }
    Class<?> clazz;
    try {
      clazz = Class.forName(className, false, classLoader);
      // Skip interface and abstract class
      if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
        return false;
      }
    } catch (Throwable t) {
      LOG.trace("Class: {} can not be found", className, t);
      return false;
    }
    return EngineConnPluginUtils.PLUGIN_PARENT_CLASS.isAssignableFrom(clazz);
  }
}
