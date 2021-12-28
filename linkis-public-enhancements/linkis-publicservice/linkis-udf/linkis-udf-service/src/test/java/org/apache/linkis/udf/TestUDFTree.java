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
 
package org.apache.linkis.udf;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class TestUDFTree {

    public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException {
        File hiveDependency = new File("repository\\org\\apache\\hive\\hive-exec\\1.2.1\\hive-exec-1.2.1.jar");
        File jar = new File("E:\\tm_client_1.6.jar");
        URL[] url = {new URL("file:" + jar.getAbsolutePath()), new URL("file:" + hiveDependency.getAbsolutePath())};
        URLClassLoader loader = URLClassLoader.newInstance(url);
        Class clazz = loader.loadClass("org.apache.linkis.mask.udf.BdpAddressFirstEightMask");
        Constructor constructor = clazz.getConstructor(new Class[0]);
        Modifier.isPublic(constructor.getModifiers());
    }
}
