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

package com.webank.wedatasphere.linkis.engine.pipeline.util;

import java.util.regex.Pattern;

/**
 * Created by johnnwang on 2019/1/30.
 */
public class PipeLineUtil {
    public static final String SET_REGEX = "\\s*set\\s+([a-z]{2,12})\\s*=\\s*(\\S+)\\s?";
    public static final String STORAGE_REGEX = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?";
    static final Pattern SET_PATTERN = Pattern.compile(SET_REGEX);
    static final Pattern STORAGE_PATTERN = Pattern.compile(STORAGE_REGEX);

    public static String getSourcePath(String code){
        return STORAGE_PATTERN.matcher(code).group(1);
    }
}
