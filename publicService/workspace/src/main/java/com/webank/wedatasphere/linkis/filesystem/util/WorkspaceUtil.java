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

package com.webank.wedatasphere.linkis.filesystem.util;

import com.webank.wedatasphere.linkis.filesystem.exception.WorkSpaceException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * Created by johnnwang on 2018/11/5.
 */
public class WorkspaceUtil {
    private static String[] namenodes;
    private static String linuxUserManagerParentPath;

    public static String infoReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*INFO(.*)";
    public static String warnReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*WARN(.*)";
    public static String errorReg = "((19|20)[0-9]{2})-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01]) "
            + "([01]?[0-9]|2[0-3]):[0-5][0-9]:[0-5][0-9]" +"\\.\\d{3}\\s*ERROR(.*)";

    public static String getOpenFileTypeByFileName(String path) throws WorkSpaceException {
        if (StringUtils.isEmpty(path)) {
        }
        if (path.endsWith(".sql")
                || path.endsWith(".hql")
                || path.endsWith(".txt")
                || path.endsWith(".python")
                || path.endsWith(".log")
                || path.endsWith(".r")
                || path.endsWith(".out")
                || path.endsWith(".scala")
                || path.endsWith(".py")
                || path.endsWith(".mlsql")
        ) {
            return "script";
        } else if (path.endsWith(".dolphin")) {
            return "resultset";
        } else {
            throw new WorkSpaceException("unsupported type!");
        }
    }

    public static Boolean logMatch(String code ,String pattern){
        return Pattern.matches(pattern,code);
    }
}
