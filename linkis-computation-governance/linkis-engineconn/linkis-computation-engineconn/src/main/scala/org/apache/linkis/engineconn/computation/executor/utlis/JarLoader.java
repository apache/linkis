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

package org.apache.linkis.engineconn.computation.executor.utlis;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JarLoader extends URLClassLoader {
  private AccessControlContext acc;

  public JarLoader(String[] paths) {
    this(paths, false);
  }

  public JarLoader(String[] paths, boolean recursive) {
    this(paths, recursive, JarLoader.class.getClassLoader());
  }

  public JarLoader(String[] paths, boolean recursive, ClassLoader parent) {
    super(getURLs(paths, recursive), parent);
  }

  private static URL[] getURLs(String[] paths, boolean recursive) {
    List<URL> urls = new ArrayList<>();
    if (recursive) {
      List<String> dirs = new ArrayList<>();
      for (String path : paths) {
        dirs.add(path);
        collectDirs(path, dirs);
      }
      for (String path : dirs) {
        urls.addAll(doGetURLs(path));
      }
    } else {
      // For classpath, classloader will recursive automatically
      urls.addAll(
          Arrays.stream(paths)
              .map(File::new)
              .filter(File::exists)
              .map(
                  f -> {
                    try {
                      return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                      // Ignore
                      return null;
                    }
                  })
              .collect(Collectors.toList()));
    }
    return urls.toArray(new URL[0]);
  }

  public void addJarURL(String path) {
    // Single jar
    File singleJar = new File(path);
    if (singleJar.exists() && singleJar.isFile()) {
      try {
        this.addURL(singleJar.toURI().toURL());
      } catch (MalformedURLException e) {
        // Ignore
      }
    }
  }

  private static void collectDirs(String path, List<String> collector) {

    File current = new File(path);
    if (!current.exists() || !current.isDirectory()) {
      return;
    }

    if (null != current.listFiles()) {
      for (File child : Objects.requireNonNull(current.listFiles())) {
        if (!child.isDirectory()) {
          continue;
        }

        collector.add(child.getAbsolutePath());
        collectDirs(child.getAbsolutePath(), collector);
      }
    }
  }

  private static List<URL> doGetURLs(final String path) {

    File jarPath = new File(path);

    Validate.isTrue(jarPath.exists() && jarPath.isDirectory(), "jar包路径必须存在且为目录.");

    /* set filter */
    FileFilter jarFilter = pathname -> pathname.getName().endsWith(".jar");

    /* iterate all jar */
    File[] allJars = new File(path).listFiles(jarFilter);
    assert allJars != null;
    List<URL> jarURLs = new ArrayList<>(allJars.length);

    for (File allJar : allJars) {
      try {
        jarURLs.add(allJar.toURI().toURL());
      } catch (Exception e) {
        // Ignore
      }
    }

    return jarURLs;
  }

  /**
   * change the order to load class
   *
   * @param name name
   * @param resolve isResolve
   * @return
   * @throws ClassNotFoundException
   */
  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      // First, check if the class has already been loaded
      Class<?> c = findLoadedClass(name);
      if (c == null) {
        long t0 = System.nanoTime();
        try {
          // invoke findClass in this class
          c = findClass(name);
        } catch (ClassNotFoundException e) {
          // ClassNotFoundException thrown if class not found
        }
        if (c == null) {
          return super.loadClass(name, resolve);
        }
        // For compatibility with higher versions > java 1.8.0_141
        //                sun.misc.PerfCounter.getFindClasses().addElapsedTimeFrom(t0);
        //                sun.misc.PerfCounter.getFindClasses().increment();
      }
      if (resolve) {
        resolveClass(c);
      }
      return c;
    }
  }
}
