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
 
package org.apache.linkis.cli.application.interactor.command.template;

import org.apache.linkis.cli.application.constants.LinkisClientKeys;
import org.apache.linkis.cli.application.interactor.command.LinkisCmdType;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.CommandException;
import org.apache.linkis.cli.core.exception.ValidateException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.command.template.AbstractCmdTemplate;
import org.apache.linkis.cli.core.interactor.command.template.option.Flag;
import org.apache.linkis.cli.core.interactor.command.template.option.MapOption;
import org.apache.linkis.cli.core.interactor.command.template.option.SpecialMapOption;
import org.apache.linkis.cli.core.interactor.command.template.option.StdOption;

/**
 * @description: A command that is compatible with any types of Linkis' Job.
 */
public class UniversalCmdTemplate extends AbstractCmdTemplate {

    protected StdOption<String> gatewayUrl = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_COMMON_GATEWAY_URL, new String[]{"--gatewayUrl"},
            "specify linkis gateway url",
            true, "");
    protected StdOption<String> authenticatationStrategy = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_COMMON_AUTHENTICATION_STRATEGY, new String[]{"--authStg"},
            "specify linkis authentication strategy",
            true, "");
    protected StdOption<String> authKey = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_COMMON_TOKEN_KEY, new String[]{"--authKey"},
            "specify linkis authentication key(tokenKey)",
            true, "");
    protected StdOption<String> authValue = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_COMMON_TOKEN_VALUE, new String[]{"--authVal"},
            "specify linkis authentication value(tokenValue)",
            true, "");
    protected StdOption<String> userConfigPath = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_CLIENT_USER_CONFIG, new String[]{"--userConf"},
            "specify user configuration file path(absolute)",
            true, "");
    protected StdOption<String> killOpt = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_CLIENT_KILL_OPT, new String[]{"--kill"},
            "specify linkis taskId for job to be killed",
            true, "");
    protected StdOption<String> statusOpt = option(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_CLIENT_STATUS_OPT, new String[]{"--status"},
            "specify linkis taskId for querying job status",
            true, "");
    protected Flag helpOpt = flag(LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_CLIENT_HELP_OPT, new String[]{"--help"},
            "specify linkis taskId for querying job status",
            true, false);

    protected StdOption<String> engineTypeOP = option(
            LinkisClientKeys.JOB_LABEL, LinkisClientKeys.JOB_LABEL_ENGINE_TYPE,
            new String[]{"-engineType"},
            "specify linkis engineType for this job",
            true, "");

    protected StdOption<String> codeTypeOp = option(
            LinkisClientKeys.JOB_LABEL, LinkisClientKeys.JOB_LABEL_CODE_TYPE,
            new String[]{"-codeType"},
            "specify linkis runType for this job",
            true, "");

    protected StdOption<String> codeOp = option(
            LinkisClientKeys.JOB_EXEC, LinkisClientKeys.JOB_EXEC_CODE,
            new String[]{"-code"},
            "specify code that you want to execute",
            true, "");

    protected StdOption<String> codePathOp = option(
            LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.JOB_COMMON_CODE_PATH,
            new String[]{"-codePath"},
            "specify file path that contains code you want to execute",
            true, "");

    protected StdOption<String> scriptPathOp = option(
            LinkisClientKeys.JOB_SOURCE, LinkisClientKeys.JOB_SOURCE_SCRIPT_PATH,
            new String[]{"-scriptPath"},
            "specify remote path for your uploaded script",
            true, "");

    protected StdOption<String> submitUser = option(
            LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.JOB_COMMON_SUBMIT_USER,
            new String[]{"-submitUser"},
            "specify submit user for this job",
            true, "");

    protected StdOption<String> proxyUser = option(
            LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.JOB_COMMON_PROXY_USER,
            new String[]{"-proxyUser"},
            "specify proxy user who executes your code in Linkis server-side",
            true, "");

    protected StdOption<String> creatorOp = option(
            LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.JOB_COMMON_CREATOR,
            new String[]{"-creator"},
            "specify creator for this job",
            true, "");

    protected StdOption<String> outPathOp = option(
            LinkisClientKeys.LINKIS_CLIENT_COMMON, LinkisClientKeys.LINKIS_CLIENT_COMMON_OUTPUT_PATH,
            new String[]{"-outPath"},
            "specify output path for resultSet. If not specified, then output reset to screen(stdout)",
            true, "");

    protected MapOption confMapOp = mapOption(
            LinkisClientKeys.JOB_PARAM_CONF, LinkisClientKeys.JOB_PARAM_CONF,
            new String[]{"-confMap"},
            "specify configurationMap(startupMap) for your job. You can put any start-up parameters into this Map(e.g. spark.executor.instances). Input format: -confMap key1=value1 -confMap key2=value2",
            true);

    protected SpecialMapOption varMapOp = speciaMapOption(
            LinkisClientKeys.JOB_PARAM_VAR, LinkisClientKeys.JOB_PARAM_VAR,
            new String[]{"-varMap"},
            "specify variables map. Variables is for key-word substitution. Use \'${key}\' to specify key-word. Input substitution rule as follow: -varMap key1=value1 -varMap key2=value2",
            true);

    protected MapOption labelMapOp = mapOption(
            LinkisClientKeys.JOB_LABEL, LinkisClientKeys.JOB_LABEL,
            new String[]{"-labelMap"},
            "specify label map. You can put any Linkis into this Map. Input format: -labelMap labelName1=labelValue1 -labelMap labelName2=labelValue2",
            true);

    protected MapOption sourceMapOp = mapOption(
            LinkisClientKeys.JOB_SOURCE, LinkisClientKeys.JOB_SOURCE,
            new String[]{"-sourceMap"},
            "specify source map. Input format: -sourceMap key1=value1 -sourceMap key2=value2",
            true);


    public UniversalCmdTemplate() {
        super(LinkisCmdType.UNIVERSAL);
    }

    @Override
    public void checkParams() throws CommandException {
        int cnt = 0;
        if (statusOpt.hasVal()) {
            cnt++;
        }
        if (killOpt.hasVal()) {
            cnt++;
        }
        if (helpOpt.hasVal()) {
            cnt++;
        }
        if (cnt > 1) {
            throw new ValidateException("VLD0001", ErrorLevel.ERROR, CommonErrMsg.ValidationErr,
                    "Can only specify 1 of: " + statusOpt.getParamName() + "/" +
                            killOpt.getParamName() + "/" + helpOpt.getParamName() + "/"
            );
        } else if (cnt == 0) {
            if (codeOp.hasVal() && codePathOp.hasVal()) {
                throw new ValidateException("VLD0001", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, codeOp.getParamName() + " and " + codePathOp.getParamName() + " should not be non-empty at the same time");

            }
            if (!codeOp.hasVal() && !codePathOp.hasVal()) {
                throw new ValidateException("VLD0001", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, codeOp.getParamName() + " and " + codePathOp.getParamName() + " should not be empty at the same time");
            }
        }
    }
}