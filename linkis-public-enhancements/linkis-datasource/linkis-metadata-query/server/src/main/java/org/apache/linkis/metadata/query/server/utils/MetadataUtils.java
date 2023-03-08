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

package org.apache.linkis.metadata.query.server.utils;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.BaseMetadataService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.metadata.query.common.errorcode.LinkisMetadataQueryErrorCodeSummary.*;

/** Metadata Utils */
public class MetadataUtils {

  private static final String JAR_SUF_NAME = ".jar";

  private static final String CLASS_SUF_NAME = ".class";

  private static final Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);

  public static final String NAME_REGEX =
      CommonVars.apply("linkis.metadata.query.regex", "^[a-zA-Z\\-\\d_\\.=/:,]+$").getValue();

  public static final Pattern nameRegexPattern = Pattern.compile(NAME_REGEX);

  /**
   * Get the primitive class
   *
   * @param clazz class
   * @return return
   */
  public static Class<?> getPrimitive(Class<?> clazz) {
    try {
      Class<?> primitive = null;
      if (clazz.isPrimitive()) {
        primitive = clazz;
      } else {
        Class<?> innerType = ((Class<?>) clazz.getField("TYPE").get(null));
        if (innerType.isPrimitive()) {
          primitive = innerType;
        }
      }
      return primitive;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      return null;
    }
  }

  public static BaseMetadataService loadMetaService(
      Class<? extends BaseMetadataService> metaServiceClass, ClassLoader metaServiceClassLoader) {
    ClassLoader storeClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(metaServiceClassLoader);
    try {
      final Constructor<?>[] constructors = metaServiceClass.getConstructors();
      if (constructors.length <= 0) {
        throw new MetaRuntimeException(
            MessageFormat.format(NO_CONSTRUCTOR_SERVICE.getErrorDesc(), metaServiceClass.getName()),
            null);
      }
      List<Constructor<?>> acceptConstructor =
          Arrays.stream(constructors)
              .filter(constructor -> constructor.getParameterCount() == 0)
              .collect(Collectors.toList());
      if (acceptConstructor.size() > 0) {
        // Choose the first one
        Constructor<?> constructor = acceptConstructor.get(0);
        try {
          return (BaseMetadataService) constructor.newInstance();
        } catch (Exception e) {
          throw new MetaRuntimeException(
              MessageFormat.format(UNABLE_META_SERVICE.getErrorDesc(), metaServiceClass.getName()),
              e);
        }
      } else {
        throw new MetaRuntimeException(
            MessageFormat.format(ILLEGAL_META_SERVICE.getErrorDesc(), metaServiceClass.getName()),
            null);
      }
    } finally {
      Thread.currentThread().setContextClassLoader(storeClassLoader);
    }
  }
  /**
   * Search meta service class from classloader
   *
   * @param serviceClassLoader service class loader
   * @return collect
   */
  public static String[] searchMetaServiceClassInLoader(URLClassLoader serviceClassLoader) {
    URL[] urlsOfClassLoader = serviceClassLoader.getURLs();
    List<String> classNameList = new ArrayList<>();
    for (URL url : urlsOfClassLoader) {
      String pathForUrl = url.getPath();
      List<String> searchResult =
          searchMetaServiceClassFormURI(
              pathForUrl, className -> isSubMetaServiceClass(className, serviceClassLoader));
      if (Objects.nonNull(searchResult)) {
        classNameList.addAll(searchResult);
      }
    }
    return classNameList.toArray(new String[] {});
  }

  public static Class<? extends BaseMetadataService> loadMetaServiceClass(
      ClassLoader classLoader, String className, boolean initialize, String notFoundMessage) {
    // Try to load use expectClassName
    try {
      return Class.forName(className, initialize, classLoader)
          .asSubclass(BaseMetadataService.class);
    } catch (ClassNotFoundException ne) {
      LOG.warn(notFoundMessage);
    }
    return null;
  }

  private static List<String> searchMetaServiceClassFormURI(
      String url, Function<String, Boolean> acceptedFunction) {
    List<String> classNameList = new ArrayList<>();
    if (url.endsWith(CLASS_SUF_NAME)) {
      String className = url.substring(0, url.lastIndexOf(CLASS_SUF_NAME));
      int splitIndex = className.lastIndexOf(IOUtils.DIR_SEPARATOR);
      if (splitIndex >= 0) {
        className = className.substring(splitIndex);
      }
      if (acceptedFunction.apply(className)) {
        classNameList.add(className);
      }
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
              classNameList.add(className);
            }
          }
        }
      } catch (IOException e) {
        // Trace
        LOG.trace("Fail to parse jar file:[" + url + "] in service classpath", e);
        return classNameList;
      }
    }
    return classNameList;
  }

  private static boolean isSubMetaServiceClass(String className, ClassLoader serviceClassLoader) {
    if (StringUtils.isEmpty(className)) {
      return false;
    }
    Class<?> clazz;
    try {
      clazz = Class.forName(className, false, serviceClassLoader);
      // Skip interface and abstract class
      if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) {
        return false;
      }
    } catch (Throwable t) {
      LOG.trace("Class: {} can not be found", className, t);
      return false;
    }
    return BaseMetadataService.class.isAssignableFrom(clazz);
  }
}
