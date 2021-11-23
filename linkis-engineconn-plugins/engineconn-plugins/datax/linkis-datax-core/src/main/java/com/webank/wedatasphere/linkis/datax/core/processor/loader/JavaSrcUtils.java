
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

package com.webank.wedatasphere.linkis.datax.core.processor.loader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author davidhua
 * 2019/8/25
 */
public class JavaSrcUtils {
    private static final Pattern JAVA_FILE_NAME_PATTERN = Pattern.compile(
            "([\\s]+public|^public)\\s+class[\\s]+([\\S]+)[^{]+\\{[\\s\\S]+}[\\s]*$");

    private static final Pattern JAVA_PACKAGE_NAME_PATTERN = Pattern.compile(
            "^[\\s]*package\\s+[\\S]+;");

    public static String parseJavaFileName(String javaCode){
        Matcher matcher = JAVA_FILE_NAME_PATTERN.matcher(javaCode);
        if(matcher.find()){
            return matcher.group(2);
        }
        return null;
    }

    public static String addPackageName(String javaCode, String packageName){
        Matcher matcher = JAVA_PACKAGE_NAME_PATTERN.matcher(javaCode);
        StringBuffer sb = new StringBuffer();
        if(matcher.find()){
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return "package " + packageName + ";\n" + sb.toString();
    }

}
