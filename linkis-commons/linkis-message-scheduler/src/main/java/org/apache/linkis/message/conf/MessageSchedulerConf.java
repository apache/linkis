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

package org.apache.linkis.message.conf;

import org.apache.linkis.common.conf.CommonVars;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

public class MessageSchedulerConf {

    public static final String SERVICE_SCAN_PACKAGE =
            CommonVars.apply("wds.linkis.ms.service.scan.package", "org.apache.linkis").getValue();
    public static final Integer MAX_PARALLELISM_CONSUMERS =
            CommonVars.apply("wds.linkis.ms.parallelism.consumer.max", 200).getValue();
    public static final Integer MAX_PARALLELISM_USER =
            CommonVars.apply("wds.linkis.ms.parallelism.user.count", 10).getValue();
    public static final Integer MAX_RUNNING_JOB =
            CommonVars.apply("wds.linkis.ms.running.jobs.max", 5 * MAX_PARALLELISM_USER).getValue();
    public static final Integer MAX_QUEUE_CAPACITY =
            CommonVars.apply("wds.linkis.ms.queue.capacity.max", MAX_RUNNING_JOB * 100).getValue();

    public static final Reflections REFLECTIONS =
            new Reflections(
                    SERVICE_SCAN_PACKAGE,
                    new MethodAnnotationsScanner(),
                    new TypeAnnotationsScanner(),
                    new SubTypesScanner());

    public static final String USER_KEY = "_username_";
    public static final String REQUEST_KEY = "_req_";
    public static final String RESULT_KEY = "_result_";
    public static final String CONTEXT_KEY = "_context_";
    public static final String SENDER_KEY = "_sender_";
    public static final String TIMEOUT_POLICY = "_timeout_policy_";
    public static final String DURATION_KEY = "_duration_";
}
