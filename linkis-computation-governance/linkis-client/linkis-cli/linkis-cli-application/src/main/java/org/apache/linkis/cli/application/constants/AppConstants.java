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
 
package org.apache.linkis.cli.application.constants;

public class AppConstants {

    public static final String DEFAULT_CONFIG_NAME = "linkis-cli.properties";
    public static final Long JOB_QUERY_SLEEP_MILLS = 2000l;
    public static final String RESULTSET_BEGIN_LOGO = "============RESULT SET============";
    public static final String RESULTSET_END_LOGO = "============END OF RESULT SET============";

    public static final String RESULTSET_META_BEGIN_LOGO = "-----------RESULT SET META DATA------------";
    public static final String RESULTSET_META_END_LOGO = "------------END OF RESULT SET META DATA------------";

    public static final String RESULTSET_CONTENT_BEGIN_LOGO = "------------RESULT SET CONTENT------------";
    public static final String RESULTSET_CONTENT_END_LOGO = "------------END OF RESULT SET CONTENT------------";

    public static final String RESULTSET_SEPARATOR_LOGO = "------------------------";


    public static final String JOB_ID_PREFIX = "";
    public static final Integer RESULTSET_PAGE_SIZE = 5000;

    public static final String KILL_SUCCESS = "kill.success";
    public static final String KILL_MSG = "kill.msg";
}
