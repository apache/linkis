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
 
package org.apache.linkis.cli.common.entity.context;

import org.apache.linkis.cli.common.entity.command.CmdTemplate;
import org.apache.linkis.cli.common.entity.command.CmdType;
import org.apache.linkis.cli.common.entity.command.Params;
import org.apache.linkis.cli.common.entity.execution.executor.Executor;
import org.apache.linkis.cli.common.entity.job.Job;
import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.common.entity.version.ClientVersionInformation;

import java.util.List;
import java.util.Map;

/**
 * @description: storing intermediate data-structure, all get methods should return null if not exist
 * Probably not going to use this
 */
public interface LinkisClientContext {

    String getPrimaryTemplateName();

    void setPrimaryTemplateName(String primaryTemplateName);

    List<CmdType> getAllSupportedCommandTypesAsList();

    void putSupportedCommandType(CmdType cmdType);

    void removeSupportedCommandTypes(CmdType cmdType);

    void clearSupportedCommandType();

    /**
     * CmdTemplate for parsing
     */
    void putCommandTemplate(CmdTemplate template);

    void removeCommandTemplate(String commandTypeName);

    CmdTemplate getCommandTemplate(String commandTypeName);

    Map<String, CmdTemplate> getTemplatesAsMap();

    void clearCommandTemplates();

    /**
     * ParsedTemplateCopy for validation, should be a deep copy of {@link CmdTemplate} instance
     */
    void putParsedTemplateCopy(String identifier, CmdTemplate template);

    void removeParsedTemplateCopy(String identifier);

    CmdTemplate getParsedTemplateCopy(String identifier);

    Map<String, CmdTemplate> getAllParsedTemplateCopiesAsMap();

    void clearParsedTemplateCopies();

    /**
     * Params for submitting jobs
     */
    Params getCommandParam(String identifier);

    Map<String, Params> getAllCommandParamsAsMap();

    void putCommandParam(Params param);

    void removeCommandParam(String identifier);

    void clearCommandParams();

    /**
     * For Config, SYS_ENV, SYS_PROP or anything in the form of kv-pair
     */
    Map<String, ClientProperties> getAllPropertiesAsMap();

    ClientProperties getProperties(String identifier);

    void putClientProperties(ClientProperties clientProperties);

    void removeProperties(String identifier);

    void clearProperties();

    /**
     * Version info
     */
    ClientVersionInformation getClientVersionInformation();

    void setClientVersionInformation(ClientVersionInformation clientVersionInformation);

    /**
     * Executor
     */
    Executor getExecutor(String identifier);

    void putExecutor(Executor executor);

    Map<String, Executor> getAllExecutorAsMap();

    void removeExecutor(String identifier);

    void clearExecutors();

    /**
     * Job
     */
    Job getJob(String identifier);

    void putJob(Job Job);

    Map<String, Job> getAllJobsAsMap();

    void removeJob(String identifier);

    void clearJobs();
}