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

package org.apache.linkis.metadata.query.server.loader;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.ErrorException;
import org.apache.linkis.metadata.query.common.exception.MetaRuntimeException;
import org.apache.linkis.metadata.query.common.service.AbstractMetaService;
import org.apache.linkis.metadata.query.common.service.MetadataService;
import org.apache.linkis.metadata.query.server.utils.MetadataUtils;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/** Class Loader for metaClass // TODO used interface class */
public class MetaClassLoaderManager {

    private final Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();

    private final Map<String, MetaServiceInstance> metaServiceInstances = new ConcurrentHashMap<>();

    public static CommonVars<String> LIB_DIR =
            CommonVars.apply(
                    "wds.linkis.server.mdm.service.lib.dir",
                    Configuration.getLinkisHome()
                            + "/lib/linkis-public-enhancements/linkis-ps-metadataquery/service");
    public static CommonVars<Integer> INSTANCE_EXPIRE_TIME =
            CommonVars.apply("wds.linkis.server.mdm.service.instance.expire-in-seconds", 60);

    private static final String META_CLASS_NAME =
            "org.apache.linkis.metadata.query.service.%sMetaService";

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
                                            + TimeUnit.MILLISECONDS.convert(
                                                    expireTimeInSec, TimeUnit.SECONDS))
                                    < System.currentTimeMillis();
        }
        if (needToLoad) {
            MetaServiceInstance finalServiceInstance1 = serviceInstance;
            serviceInstance =
                    metaServiceInstances.compute(
                            dsType,
                            (key, instance) -> {
                                if (null != instance
                                        && !Objects.equals(finalServiceInstance1, instance)) {
                                    return instance;
                                }
                                String lib = LIB_DIR.getValue();
                                String stdLib = lib.endsWith("/") ? lib.replaceAll(".$", "") : lib;
                                String componentLib = stdLib + "/" + dsType;
                                LOG.info(
                                        "Start to load/reload meta instance of data source type: ["
                                                + dsType
                                                + "] from library dir:"
                                                + componentLib);
                                ClassLoader parentClassLoader =
                                        MetaClassLoaderManager.class.getClassLoader();
                                ClassLoader metaClassLoader =
                                        classLoaders.compute(
                                                dsType,
                                                (type, classLoader) -> {
                                                    try {
                                                        return new URLClassLoader(
                                                                getJarsUrlsOfPath(componentLib)
                                                                        .toArray(new URL[0]),
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
                                            "Error in creating classloader of type: ["
                                                    + dsType
                                                    + "]",
                                            null);
                                }
                                String expectClassName = null;
                                if (dsType.length() > 0) {
                                    String prefix =
                                            dsType.substring(0, 1).toUpperCase()
                                                    + dsType.substring(1);
                                    expectClassName = String.format(META_CLASS_NAME, prefix);
                                }
                                Class<? extends MetadataService> metaServiceClass =
                                        searchForLoadMetaServiceClass(
                                                metaClassLoader, expectClassName, true);
                                if (Objects.isNull(metaServiceClass)) {
                                    throw new MetaRuntimeException(
                                            "Fail to init and load meta service class for type: ["
                                                    + dsType
                                                    + "]",
                                            null);
                                }
                                MetadataService metadataService =
                                        MetadataUtils.loadMetaService(
                                                metaServiceClass, metaClassLoader);
                                if (metadataService instanceof AbstractMetaService) {
                                    LOG.info(
                                            "Invoke the init() method in meta service for type: ["
                                                    + dsType
                                                    + "]");
                                    ((AbstractMetaService<?>) metadataService).init();
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
                Method method =
                        Arrays.stream(childMethods)
                                .filter(eachMethod -> eachMethod.getName().equals(m))
                                .collect(Collectors.toList())
                                .get(0);
                return method.invoke(finalServiceInstance.serviceInstance, args);
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

    private Class<? extends MetadataService> searchForLoadMetaServiceClass(
            ClassLoader classLoader, String expectClassName, boolean initialize) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class<? extends MetadataService> metaClass = null;
            if (StringUtils.isNotBlank(expectClassName)) {
                metaClass =
                        MetadataUtils.loadMetaServiceClass(
                                classLoader,
                                expectClassName,
                                initialize,
                                "Cannot find class in using expect class name: ["
                                        + expectClassName
                                        + "]");
            }
            if (Objects.isNull(metaClass)) {
                if (classLoader instanceof URLClassLoader) {
                    String[] metaServiceClassNames =
                            MetadataUtils.searchMetaServiceClassInLoader(
                                    (URLClassLoader) classLoader);
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
        private MetadataService serviceInstance;

        private Method[] methods;

        private ClassLoader metaClassLoader;

        private long initTimeStamp = 0L;

        public MetaServiceInstance(MetadataService serviceInstance, ClassLoader metaClassLoader) {
            this.serviceInstance = serviceInstance;
            this.metaClassLoader = metaClassLoader;
            this.methods = serviceInstance.getClass().getMethods();
            this.initTimeStamp = System.currentTimeMillis();
        }

        public MetadataService getServiceInstance() {
            return serviceInstance;
        }
    }
}
