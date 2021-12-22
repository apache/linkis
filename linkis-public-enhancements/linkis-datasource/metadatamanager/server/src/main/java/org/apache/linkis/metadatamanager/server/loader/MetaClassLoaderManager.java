package org.apache.linkis.metadatamanager.server.loader;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.exception.ErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class MetaClassLoaderManager {
    private final Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> clazzes = new ConcurrentHashMap<>();
    private final Map<String, Object> instances = new ConcurrentHashMap<>();
    public static CommonVars<String> LIB_DIR = CommonVars.apply("wds.linkis.server.mdm.service.lib.dir", "/tmp/mdm/lib");
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaClassLoaderManager.class);

    public BiFunction<String, Object[], Object> getInvoker(String dsType) throws ErrorException {
        ClassLoader extClassLoader = MetaClassLoaderManager.class.getClassLoader().getParent();
        classLoaders.computeIfAbsent(dsType,(x)->{
            String lib = LIB_DIR.getValue();
            String stdLib = lib.endsWith("/") ? lib.replaceAll(".$", "") : lib;
            String componentLib = stdLib + "/" + dsType;

            try {
                return new URLClassLoader(getJarsUrlsOfPath(componentLib).toArray(new URL[0]), extClassLoader);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        if(classLoaders.get(dsType) == null){
            throw new ErrorException(-1, "Error in creating classloader");
        }
        ClassLoader childClassLoader = classLoaders.get(dsType);
        String prefix = dsType.substring(0, 1).toUpperCase() + dsType.substring(1);
        String className = "org.apache.linkis.metadatamanager.service." + prefix + "MetaService";
        Thread.currentThread().setContextClassLoader(childClassLoader);

        clazzes.computeIfAbsent(dsType, (x)->{
            try {
                ClassLoader classLoader = classLoaders.get(dsType);
                return classLoader.loadClass(className);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        if(clazzes.get(dsType) == null){
            throw new ErrorException(-1, "Error in loading class " + className);
        }

        instances.computeIfAbsent(dsType, (x)->{
            try {
                return clazzes.get(dsType).newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
        if(instances.get(dsType) == null){
            throw new ErrorException(-1, "Error in instantiate org.apache.linkis.metadatamanager.service.MetaService");
        }


        Method[] childMethods = clazzes.get(dsType).getMethods();
        return (String m, Object...args)-> {
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Method method = Arrays.stream(childMethods)
                        .filter(eachMethod -> eachMethod.getName().equals(m)).collect(Collectors.toList()).get(0);
                return method.invoke(instances.get(dsType), args);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        };
    }


    public List<URL> getJarsUrlsOfPath(String path) throws MalformedURLException {
        File file = new File(path);
        List<URL> jars = new ArrayList<>();
        if (file.listFiles() != null){
            for(File f : file.listFiles()){
                if (!f.isDirectory() && f.getName().endsWith(".jar")){
                    jars.add(f.toURI().toURL());
                }
            }
        }
        return jars;
    }
}
