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
 
package org.apache.linkis.cli.application.interactor.validate;

import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.interactor.job.LinkisJob;
import org.apache.linkis.cli.application.interactor.job.LinkisJobMan;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ValidateException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.validate.Validator;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;


public class LinkisJobValidator implements Validator {
    @Override
    public void doValidation(Object input) throws LinkisClientRuntimeException {
        if (!(input instanceof LinkisJob || input instanceof LinkisJobMan)) {
            throw new ValidateException("VLD0007", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, "Input of LinkisJobValidator is not instance of LinkisJob nor LinkisJobMan. Type: " + input.getClass().getCanonicalName());
        }
        boolean ok = true;
        StringBuilder reasonSb = new StringBuilder();
        if (input instanceof LinkisJobMan) {
            if (StringUtils.isBlank(((LinkisJobMan) input).getJobId())) {
                reasonSb.append("jobId cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
        } else {
            LinkisJob linkisJob = (LinkisJob) input;
            if (StringUtils.isBlank(linkisJob.getSubmitUser())) {
                reasonSb.append("Submit User cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
            if (StringUtils.isBlank(linkisJob.getProxyUser())) {
                reasonSb.append("proxy(execute) User cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getSubExecutionType() == null) {
                reasonSb.append("SubExecutionType cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getLabelMap() == null) {
                reasonSb.append("labelMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getExecutionMap() == null) {
                reasonSb.append("ExecutionMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getSourceMap() == null) {
                reasonSb.append("SourceMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getParamConfMap() == null) {
                reasonSb.append("startupMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getParamVarsMap() == null) {
                reasonSb.append("variableMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            if (linkisJob.getParamRunTimeMap() == null) {
                reasonSb.append("runTimeMap cannot be null").append(System.lineSeparator());
                ok = false;
            }
            for (Map.Entry<String, Object> entry : linkisJob.getExecutionMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("ExecutionMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
            }
            for (Map.Entry<String, Object> entry : linkisJob.getLabelMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("LabelMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
                Object val = entry.getValue();
                if (val instanceof String) {
                    if (StringUtils.contains((String) val, " ")) {
                        reasonSb.append("LabelMap value cannot contains space character. key: ")
                                .append(entry.getKey()).append("value: ").append(val)
                                .append(System.lineSeparator());
                        ok = false;
                    }
                }
            }
            for (Map.Entry<String, Object> entry : linkisJob.getParamConfMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("startUpMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
                Object val = entry.getValue();
                if (val instanceof String) {
                    if (StringUtils.contains((String) val, " ")) {
                        reasonSb.append("startUpMap value cannot contains space character. key: ")
                                .append(entry.getKey()).append("value: ").append(val)
                                .append(System.lineSeparator());
                        ok = false;
                    }
                }
            }
            for (Map.Entry<String, Object> entry : linkisJob.getParamRunTimeMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("runtimeMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
                Object val = entry.getValue();
                if (val instanceof String) {
                    if (StringUtils.contains((String) val, " ")) {
                        reasonSb.append("runtimeMap value cannot contains space character. key: ")
                                .append(entry.getKey()).append("value: ").append(val)
                                .append(System.lineSeparator());
                        ok = false;
                    }
                }
            }
            for (Map.Entry<String, Object> entry : linkisJob.getParamVarsMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("variablesMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
                Object val = entry.getValue();
                if (val instanceof String) {
                    if (StringUtils.contains((String) val, " ")) {
                        reasonSb.append("variablesMap value cannot contains space character. key: ")
                                .append(entry.getKey()).append("value: ").append(val)
                                .append(System.lineSeparator());
                        ok = false;
                    }
                }
            }
            for (Map.Entry<String, Object> entry : linkisJob.getSourceMap().entrySet()) {
                if (StringUtils.contains(entry.getKey(), " ")) {
                    reasonSb.append("sourceMap key cannot contains space character. key: ").append(entry.getKey()).append(System.lineSeparator());
                    ok = false;
                }
                Object val = entry.getValue();
                if (val instanceof String) {
                    if (StringUtils.contains((String) val, " ")) {
                        reasonSb.append("sourceMap value cannot contains space character. key: ")
                                .append(entry.getKey()).append("value: ").append(val)
                                .append(System.lineSeparator());
                        ok = false;
                    }
                }
            }
            if (StringUtils.isBlank((String) linkisJob.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE))) {
                reasonSb.append(LinkisKeys.KEY_ENGINETYPE)
                        .append(" cannot be empty or blank")
                        .append(System.lineSeparator());
                ok = false;
            }
            if (StringUtils.isBlank((String) linkisJob.getLabelMap().get(LinkisKeys.KEY_CODETYPE))) {
                reasonSb.append(LinkisKeys.KEY_CODETYPE)
                        .append(" cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
            if (StringUtils.isBlank((String) linkisJob.getSourceMap().get(LinkisKeys.KEY_SCRIPT_PATH))) {
                reasonSb.append(LinkisKeys.KEY_SCRIPT_PATH)
                        .append(" cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
            if (StringUtils.isBlank((String) linkisJob.getExecutionMap().get(LinkisKeys.KEY_CODE))) {
                reasonSb.append(LinkisKeys.KEY_CODE)
                        .append(" cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            }
            if (StringUtils.isBlank((String) linkisJob.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR))) {
                reasonSb.append(LinkisKeys.KEY_USER_CREATOR)
                        .append(" cannot be empty or blank").append(System.lineSeparator());
                ok = false;
            } else {
                String userCreator = (String) linkisJob.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR);
                int idx = StringUtils.indexOf(userCreator, "-");
                if (idx == -1) {
                    reasonSb.append(LinkisKeys.KEY_USER_CREATOR)
                            .append("should contains exactly one \'-\'").append(System.lineSeparator());
                    ok = false;
                } else {
                    String user = StringUtils.substring(userCreator, 0, idx);
                    String creator = StringUtils.substring(userCreator, idx + 1);
                    if (StringUtils.isBlank(user) || StringUtils.isBlank(creator)) {
                        reasonSb.append("user or creator should not be blank").append(System.lineSeparator());
                        ok = false;
                    } else {
//          String forBiddenChars = "~!$%^&*-,./?|{}[]:;'()+=";
                        String forBiddenChars = "-";
                        if (StringUtils.containsAny(user, forBiddenChars)) {
                            reasonSb.append(LinkisKeys.KEY_USER_CREATOR)
                                    .append("should contains exactly one \'-\' and no special characters except \'_\'").append(System.lineSeparator());
                            ok = false;
                        }
                        if (StringUtils.containsAny(creator, forBiddenChars)) {
                            reasonSb.append(LinkisKeys.KEY_USER_CREATOR)
                                    .append("should contains exactly one \'-\' and no special characters except \'_\'").append(System.lineSeparator());
                            ok = false;
                        }
                    }
                }

            }
        }
        if (!ok) {
            throw new ValidateException("VLD0008", ErrorLevel.ERROR, CommonErrMsg.ValidationErr, "LinkisJob validation failed. Reason: " + reasonSb.toString());
        }
    }
}