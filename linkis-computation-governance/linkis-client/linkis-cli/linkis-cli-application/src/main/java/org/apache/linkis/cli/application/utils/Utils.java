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
 
package org.apache.linkis.cli.application.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utils {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    public static boolean isValidExecId(String execId) {
        boolean ret = false;
        if (StringUtils.isNotBlank(execId)) {
            ret = true;
        }
        return ret;
    }

    public static String progressInPercentage(float progress) {
        return String.valueOf(progress * 100) + "%";
    }

    public static void doSleepQuietly(Long sleepMills) {
        try {
            Thread.sleep(sleepMills);
        } catch (Exception ignore) {
            //ignored
        }
    }

}