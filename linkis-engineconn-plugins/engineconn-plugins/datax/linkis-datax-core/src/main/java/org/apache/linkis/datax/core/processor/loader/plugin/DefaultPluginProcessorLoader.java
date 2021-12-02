/*
 *
 *  Copyright 2020 WeBank
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.datax.core.processor.loader.plugin;

import com.alibaba.datax.common.plugin.PluginProcessorLoader;
import com.alibaba.datax.core.util.container.JarLoader;
import org.apache.linkis.datax.core.processor.loader.JavaMemoryClassObject;
import org.apache.linkis.datax.core.processor.loader.JavaMemoryFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author davidhua
 * 2019/8/26
 */
public class DefaultPluginProcessorLoader implements PluginProcessorLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginProcessorLoader.class);
    private static final String CLASS_PATH_PARAMS = "-classpath";

    @Override
    public boolean load(String fullClassName, String javaCode, String classpath) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        CustomFileManager customFileManager = new CustomFileManager(compiler.getStandardFileManager(diagnostics, null, null));
        List<JavaFileObject> javaFiles = new ArrayList<>();
        javaFiles.add(new JavaMemoryFileObject(fullClassName, javaCode));
        List<String> options = new ArrayList<>();
        options.add(CLASS_PATH_PARAMS);
        options.add(classpath);
        JavaCompiler.CompilationTask task = compiler.getTask(null, customFileManager, diagnostics, options, null, javaFiles);
        boolean success = task.call();
        if(success){
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if(classLoader instanceof JarLoader){
                JavaMemoryClassObject classObject = customFileManager.getClassObject();
                ((JarLoader)classLoader).loadClass(fullClassName, classObject.getBytes());
            }
        }else{
            //logger error
            diagnostics.getDiagnostics().forEach( diagnostic -> {
                String res = ("Code:[" + diagnostic.getCode() + "]\n") +
                        "Kind:[" + diagnostic.getKind() + "]\n" +
                        "Position:[" + diagnostic.getPosition() + "]\n" +
                        "Start Position:[" + diagnostic.getStartPosition() + "]\n" +
                        "End Position:[" + diagnostic.getEndPosition() + "]\n" +
                        "Source:[" + diagnostic.getSource() + "]\n" +
                        "Message:[" + diagnostic.getMessage(null) + "]\n" +
                        "LineNumber:[" + diagnostic.getLineNumber() + "]\n" +
                        "ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n";
                logger.error(res);
            });
        }
        return success;
    }

    @Override
    public boolean load(String fullClassName, String javaCode) {
        StringBuilder builder = new StringBuilder();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> foundClass = Class.forName(fullClassName, false, classLoader);
            if(null != foundClass){
                //The class has been loaded
                return true;
            }
        } catch (ClassNotFoundException e) {
            //Ignore try to load class
        }
        while(classLoader instanceof URLClassLoader){
            URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
            for (URL url : urlClassLoader.getURLs()) {
                builder.append(url.getFile()).append(File.pathSeparator);
            }
            classLoader = classLoader.getParent();
        }
        return load(fullClassName, javaCode, builder.toString());
    }

    private static class CustomFileManager extends ForwardingJavaFileManager{

        private JavaMemoryClassObject memoryClassObject;
        /**
         * Custom
         *
         * @param fileManager delegate to this file manager
         */
        CustomFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if(null == memoryClassObject){
                memoryClassObject = new JavaMemoryClassObject(className, kind);
            }
            return memoryClassObject;
        }

        JavaMemoryClassObject getClassObject(){
            return memoryClassObject;
        }

    }

}
