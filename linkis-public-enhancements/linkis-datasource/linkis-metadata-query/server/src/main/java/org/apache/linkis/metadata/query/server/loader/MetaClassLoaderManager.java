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

package org.apache.linkis.metadata.query.server.loader;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.metadata.query.common.cache.CacheConfiguration;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractCacheMetaService;
import org.apache.linkis.metadata.query.common.service.BaseMetadataService;
import org.apache.linkis.metadata.query.server.utils.MetadataUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.metadata.query.common.errorcode.LinkisMetadataQueryErrorCodeSummary.ERROR_IN_CREATING;
import static org.apache.linkis.metadata.query.common.errorcode.LinkisMetadataQueryErrorCodeSummary.INIT_META_SERVICE;

/** Class Loader for metaClass // TODO used interface class */
public class MetaClassLoaderManager {

  private final Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();

  private final Map<String, MetaServiceInstance> metaServiceInstances = new ConcurrentHashMap<>();

  public static CommonVars<String> LIB_DIR =
      CommonVars.apply(
          "wds.linkis.server.mdm.service.lib.dir",
          Configuration.getLinkisHome()
              + "/lib/linkis-public-enhancements/linkis-ps-publicservice/metadataquery-service");
  public static CommonVars<Integer> INSTANCE_EXPIRE_TIME =
      CommonVars.apply("wds.linkis.server.mdm.service.instance.expire-in-seconds", 60);

  private static final String META_CLASS_NAME =
      "org.apache.linkis.metadata.query.service.%sMetaService";

  private static final String MYSQL_BASE_DIR = "jdbc";

  private static final Logger LOG = LoggerFactory.getLogger(MetaClassLoaderManager.class);

  public BiFunction<String, Object[], Object> getInvoker(String dsType) throws ErrorException {
    boolean needToLoad = true;

    MetaServiceInstance serviceInstance = metaServiceInstances.get(dsType);
    if (Objects.nonNull(serviceInstance)) {
      Integer expireTimeInSec = INSTANCE_EXPIRE_TIME.getValue();
      // Lazy load
      needToLoad =
          Objects.nonNull(expireTimeInSec)
              && expireTimeInSec > 0
              && (serviceInstance.initTimeStamp
                      + TimeUnit.MILLISECONDS.convert(expireTimeInSec, TimeUnit.SECONDS))
                  < System.currentTimeMillis();
    }
    if (needToLoad) {
      MetaServiceInstance finalServiceInstance1 = serviceInstance;
      boolean isContains = CacheConfiguration.MYSQL_RELATIONSHIP_LIST.getValue().contains(dsType);
      String finalBaseType = isContains ? MYSQL_BASE_DIR : dsType;
      serviceInstance =
          metaServiceInstances.compute(
              dsType,
              (key, instance) -> {
                if (null != instance && !Objects.equals(finalServiceInstance1, instance)) {
                  return instance;
                }
                String lib = LIB_DIR.getValue();
                String stdLib = lib.endsWith("/") ? lib.replaceAll(".$", "") : lib;
                String componentLib = stdLib + "/" + finalBaseType;
                LOG.info(
                    "Start to load/reload meta instance of data source type: ["
                        + dsType
                        + "] from library dir:"
                        + componentLib);
                ClassLoader parentClassLoader = MetaClassLoaderManager.class.getClassLoader();
                ClassLoader metaClassLoader =
                    classLoaders.computeIfAbsent(
                        dsType,
                        (type) -> {
                          try {
                            return new URLClassLoader(
                                getJarsUrlsOfPath(componentLib).toArray(new URL[0]),
                                parentClassLoader);
                          } catch (Exception e) {
                            LOG.error(
                                "Cannot init the classloader of type: ["
                                    + dsType
                                    + "] in library path: ["
                                    + componentLib
                                    + "]",
                                e);
                            return null;
                          }
                        });
                if (Objects.isNull(metaClassLoader)) {
                  throw new MetaRuntimeException(
                      MessageFormat.format(ERROR_IN_CREATING.getErrorDesc(), dsType), null);
                }
                String expectClassName = null;
                if (dsType.length() > 0) {
                  String prefix = dsType.substring(0, 1).toUpperCase() + dsType.substring(1);
                  expectClassName = String.format(META_CLASS_NAME, prefix);
                }
                Class<? extends BaseMetadataService> metaServiceClass =
                    searchForLoadMetaServiceClass(metaClassLoader, expectClassName, true);
                if (Objects.isNull(metaServiceClass)) {
                  throw new MetaRuntimeException(
                      MessageFormat.format(INIT_META_SERVICE.getErrorDesc(), dsType), null);
                }
                BaseMetadataService metadataService =
                    MetadataUtils.loadMetaService(metaServiceClass, metaClassLoader);
                if (metadataService instanceof AbstractCacheMetaService) {
                  LOG.info("Invoke the init() method in meta service for type: [" + dsType + "]");
                  ((AbstractCacheMetaService<?>) metadataService).init();
                }
                return new MetaServiceInstance(metadataService, metaClassLoader);
              });
    }
    Method[] childMethods = serviceInstance.methods;
    MetaServiceInstance finalServiceInstance = serviceInstance;
    return (String m, Object... args) -> {
      ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader(finalServiceInstance.metaClassLoader);
        List<Method> methodsMatched =
            Arrays.stream(childMethods)
                .filter(
                    eachMethod -> {
                      if (eachMethod.getName().equals(m)) {
                        Class<?>[] parameterType = eachMethod.getParameterTypes();
                        if (parameterType.length == args.length) {
                          for (int i = 0; i < parameterType.length; i++) {
                            if (Objects.nonNull(args[i])) {
                              boolean matches =
                                  parameterType[i].isAssignableFrom(args[i].getClass())
                                      || ((args[i].getClass().isPrimitive()
                                              || parameterType[i].isPrimitive())
                                          && MetadataUtils.getPrimitive(args[i].getClass())
                                              == MetadataUtils.getPrimitive(parameterType[i]));
                              if (!matches) {
                                return false;
                              }
                            }
                          }
                          return true;
                        }
                      }
                      return false;
                    })
                .collect(Collectors.toList());
        if (methodsMatched.isEmpty()) {
          String type = null;
          if (Objects.nonNull(args)) {
            type =
                Arrays.stream(args)
                    .map(arg -> Objects.nonNull(arg) ? arg.getClass().toString() : "null")
                    .collect(Collectors.joining(","));
          }
          String message =
              "Unknown method: [ name: "
                  + m
                  + ", type: ["
                  + type
                  + "]] for meta service instance: ["
                  + finalServiceInstance.getServiceInstance().toString()
                  + "]";
          LOG.warn(message);
          throw new MetaRuntimeException(message, null);
        } else if (methodsMatched.size() > 1) {
          LOG.warn(
              "Find multiple matched methods with name: ["
                  + m
                  + "] such as: \n"
                  + methodsMatched.stream()
                      .map(
                          method ->
                              method.getName() + ":" + Arrays.toString(method.getParameterTypes()))
                      .collect(Collectors.joining("\n"))
                  + "\n in meta service instance: ["
                  + finalServiceInstance.getServiceInstance().toString()
                  + "], will choose the first one");
        }
        return methodsMatched.get(0).invoke(finalServiceInstance.serviceInstance, args);
      } catch (Exception e) {
        Throwable t = e;
        // UnWrap the Invocation target exception
        while (t instanceof InvocationTargetException) {
          t = t.getCause();
        }
        String message =
            "Fail to invoke method: ["
                + m
                + "] in meta service instance: ["
                + finalServiceInstance.serviceInstance.toString()
                + "]";
        LOG.warn(message, t);
        throw new MetaRuntimeException(message, t);
      } finally {
        Thread.currentThread().setContextClassLoader(currentClassLoader);
      }
    };
  }

  private Class<? extends BaseMetadataService> searchForLoadMetaServiceClass(
      ClassLoader classLoader, String expectClassName, boolean initialize) {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(classLoader);
    try {
      Class<? extends BaseMetadataService> metaClass = null;
      if (StringUtils.isNotBlank(expectClassName)) {
        metaClass =
            MetadataUtils.loadMetaServiceClass(
                classLoader,
                expectClassName,
                initialize,
                "Cannot find class in using expect class name: [" + expectClassName + "]");
      }
      if (Objects.isNull(metaClass)) {
        if (classLoader instanceof URLClassLoader) {
          String[] metaServiceClassNames =
              MetadataUtils.searchMetaServiceClassInLoader((URLClassLoader) classLoader);
          if (metaServiceClassNames.length > 0) {
            String metaServiceClassName = metaServiceClassNames[0];
            metaClass =
                MetadataUtils.loadMetaServiceClass(
                    classLoader,
                    metaServiceClassName,
                    initialize,
                    "Cannot load class in canonical name: ["
                        + metaServiceClassName
                        + "], please check the compiled jar/file");
          }
        }
      }
      return metaClass;
    } finally {
      Thread.currentThread().setContextClassLoader(currentClassLoader);
    }
  }

  private List<URL> getJarsUrlsOfPath(String path) throws MalformedURLException {
    File file = new File(path);
    List<URL> jars = new ArrayList<>();
    if (file.listFiles() != null) {
      for (File f : Objects.requireNonNull(file.listFiles())) {
        if (!f.isDirectory() && f.getName().endsWith(".jar")) {
          jars.add(f.toURI().toURL());
        } else if (f.isDirectory()) {
          jars.addAll(getJarsUrlsOfPath(f.getPath()));
        }
      }
    }
    return jars;
  }

  /** ServiceInstance Holder */
  public static class MetaServiceInstance {
    private BaseMetadataService serviceInstance;

    private Method[] methods;

    private ClassLoader metaClassLoader;

    private long initTimeStamp = 0L;

    public MetaServiceInstance(BaseMetadataService serviceInstance, ClassLoader metaClassLoader) {
      this.serviceInstance = serviceInstance;
      this.metaClassLoader = metaClassLoader;
      this.methods = serviceInstance.getClass().getMethods();
      this.initTimeStamp = System.currentTimeMillis();
    }

    public BaseMetadataService getServiceInstance() {
      return serviceInstance;
    }
  }
}
