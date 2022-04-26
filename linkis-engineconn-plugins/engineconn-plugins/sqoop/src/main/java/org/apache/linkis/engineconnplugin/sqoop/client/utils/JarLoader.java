/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.engineconnplugin.sqoop.client.utils;
import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


public class JarLoader extends URLClassLoader {

    public JarLoader(String[] paths) throws Exception {
        this(paths, JarLoader.class.getClassLoader());
    }

    public JarLoader(String[] paths, ClassLoader parent) throws Exception {
        super(getURLs(paths), parent);
    }

    private static URL[] getURLs(String[] paths) throws Exception {

        List<String> dirs = new ArrayList<String>();
        for (String path : paths) {
            dirs.add(path);
            collectDirs(path, dirs);
        }

        List<URL> urls = new ArrayList<URL>();
        for (String path : dirs) {
            urls.addAll(doGetURLs(path));
        }
        return urls.toArray(new URL[0]);
    }

    public void addURL(String path) throws MalformedURLException {
        File file = new File(path);
        if(file.isDirectory()){
            File[] subFiles = file.listFiles();
            for (File f:subFiles) {
                super.addURL(f.toURI().toURL());
            }
        }else{
            super.addURL(file.toURI().toURL());
        }

    }

    private static void collectDirs(String path, List<String> collector) {


        File current = new File(path);
        if (!current.exists() || !current.isDirectory()) {
            return;
        }

        if(null != current.listFiles()) {
            for (File child : current.listFiles()) {
                if (!child.isDirectory()) {
                    continue;
                }

                collector.add(child.getAbsolutePath());
                collectDirs(child.getAbsolutePath(), collector);
            }
        }
    }

    private static List<URL> doGetURLs(final String path) throws Exception {
        List<URL> jarURLs = new ArrayList<URL>();

        File jarPath = new File(path);

        if(!jarPath.isDirectory()){
            if(!jarPath.getName().endsWith(".jar")){
                throw new RuntimeException("The Single File Must Be Jar File");
            }
            jarURLs.add(jarPath.toURI().toURL());
            return jarURLs;
        }

        /* set filter */
        FileFilter jarFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        };

        /* iterate all jar */
        File[] allJars = new File(path).listFiles(jarFilter);


        for (int i = 0; i < allJars.length; i++) {
            jarURLs.add(allJars[i].toURI().toURL());
        }

        return jarURLs;
    }

    /**
     * change the order to load class
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)){
            //First, check if the class has already been loaded
            Class<?> c = findLoadedClass(name);
            if(c == null){
                long t0 = System.nanoTime();
                try {
                    //invoke findClass in this class
                    c = findClass(name);
                }catch(ClassNotFoundException e){
                    // ClassNotFoundException thrown if class not found
                }
                if(c == null){
                    return super.loadClass(name, resolve);
                }
                //For compatibility with higher versions > java 1.8.0_141
//                sun.misc.PerfCounter.getFindClasses().addElapsedTimeFrom(t0);
//                sun.misc.PerfCounter.getFindClasses().increment();
            }
            if(resolve){
                resolveClass(c);
            }
            return c;
        }
    }
}
