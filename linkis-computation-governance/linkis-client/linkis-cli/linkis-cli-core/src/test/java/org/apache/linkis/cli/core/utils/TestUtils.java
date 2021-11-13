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
 
package org.apache.linkis.cli.core.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class TestUtils {
    private static String replaceParas(String context, Map<String, String> m) {
        if (context == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : m.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            String key = "[#" + entry.getKey().trim() + "]";
            String value = entry.getValue().trim();

            context = StringUtils.replace(context, key, value);
        }
        return context;
    }

    public static Map<String, String> parseArgMap(String str, String separator) {

        Map<String, String> argsProps = new HashMap<>();
        String[] args = StringUtils.splitByWholeSeparator(str, separator);

        for (String arg : args) {
            int index = arg.indexOf("=");
            if (index != -1) {
                argsProps.put(arg.substring(0, index).trim(), arg.substring(index + 1).trim());
            }
        }
        return argsProps;
    }

    public static String readShellFileAndReplaceParas(String filename, String argsStr, String split) throws Exception {

        String fileContent;

        File inputFile = new File(filename);

        fileContent = FileUtils.readFileToString(inputFile);

        Map<String, String> argsMap = parseArgMap(argsStr, split);

        fileContent = replaceParas(fileContent, argsMap);

        return fileContent;
    }

}