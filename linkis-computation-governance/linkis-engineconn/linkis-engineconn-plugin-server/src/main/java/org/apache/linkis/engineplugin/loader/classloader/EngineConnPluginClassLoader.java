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

package org.apache.linkis.engineplugin.loader.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EngineConnPluginClassLoader extends URLClassLoader {

  private static final Logger logger = LoggerFactory.getLogger(EngineConnPluginClassLoader.class);

  /** Reverse order */
  private boolean reverseOrder = false;
  /** To combine other class loader */
  private List<ClassLoader> extendedLoaders = new ArrayList<>();

  public EngineConnPluginClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  public EngineConnPluginClassLoader(
      URL[] urls, ClassLoader parent, List<ClassLoader> extendedLoaders, boolean reverseOrder) {
    super(urls, parent);
    this.extendedLoaders = extendedLoaders;
    this.reverseOrder = reverseOrder;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (!extendedLoaders.isEmpty()) {
      for (ClassLoader extend : extendedLoaders) {
        try {
          return extend.loadClass(name);
        } catch (ClassNotFoundException e) {
          // ignore
        }
      }
    }
    return super.findClass(name);
  }

  /**
   * Change the order to load class (if you need)
   *
   * @param name
   * @param resolve
   * @return
   * @throws ClassNotFoundException
   */
  private Class<?> loadClassReverse(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        try {
          // invoke findClass in this class
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found
        }
        if (c == null) {
          return super.loadClass(name, resolve);
        }
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    if (reverseOrder) {
      return loadClassReverse(name, resolve);
    }
    return super.loadClass(name, resolve);
  }

  /**
   * Construct method
   *
   * @param urls classpath urls
   * @param parent parent classloader
   * @return
   */
  public static EngineConnPluginClassLoader custom(URL[] urls, ClassLoader parent) {
    return custom(urls, parent, Collections.emptyList(), false);
  }

  /**
   * Close the classloader
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    super.close();
  }

  public static EngineConnPluginClassLoader custom(
      URL[] urls, ClassLoader parent, List<ClassLoader> extendedLoaders) {
    return custom(urls, parent, extendedLoaders, false);
  }

  public static EngineConnPluginClassLoader custom(
      URL[] urls, ClassLoader parent, List<ClassLoader> extendedLoaders, boolean reverseOrder) {
    return AccessController.doPrivileged(
        (PrivilegedAction<EngineConnPluginClassLoader>)
            () ->
                new EngineConnPluginClassLoader(
                    urls, parent, Collections.emptyList(), reverseOrder));
  }
}
