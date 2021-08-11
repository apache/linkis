package com.webank.wedatasphere.linkis.metadatamanager.server.loader;

import com.google.common.base.CaseFormat;
import com.webank.wedatasphere.linkis.common.conf.CommonVars;
import com.webank.wedatasphere.linkis.common.exception.ErrorException;

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
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MetaClassLoaderManager {
    private final Map<String, ClassLoader> classLoaders = new ConcurrentHashMap<>();
    private final Map<String, Class<?>> clazzes = new ConcurrentHashMap<>();
    private final Map<String, Object> instances = new ConcurrentHashMap<>();
    public static CommonVars<String> LIB_DIR = CommonVars.apply("wds.linkis.server.mdm.service.lib.dir", "/tmp/mdm/lib");

    public BiFunction<String, Object[], Object> getInvoker(String dsType) throws ErrorException {
        classLoaders.computeIfAbsent(dsType,(x)->{
            ClassLoader extClassLoader = MetaClassLoaderManager.class.getClassLoader().getParent();

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

        String prefix = dsType.substring(0, 1).toUpperCase() + dsType.substring(1);
        String className = "com.webank.wedatasphere.linkis.metadatamanager.service." + prefix + "MetaService";
        clazzes.computeIfAbsent(dsType, (x)->{
            try {
                return classLoaders.get(dsType).loadClass(className);
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
            throw new ErrorException(-1, "Error in instantiate com.webank.wedatasphere.linkis.metadatamanager.service.MetaService");
        }

        return (String m, Object...args)-> {
            try {
                Method method = Arrays.stream(clazzes.get(dsType).getMethods())
                        .filter(eachMethod -> eachMethod.getName().equals(m)).collect(Collectors.toList()).get(0);

                return method.invoke(instances.get(dsType), args);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
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
