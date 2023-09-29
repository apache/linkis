/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.cli.application.interactor.validate;

import org.apache.linkis.cli.application.constants.LinkisKeys;
import org.apache.linkis.cli.application.interactor.job.LinkisSubmitJob;
import org.apache.linkis.cli.application.interactor.job.desc.LinkisSubmitDesc;
import org.apache.linkis.cli.common.entity.validate.Validator;
import org.apache.linkis.cli.common.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.ValidateException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class LinkisSubmitValidator implements Validator {
  @Override
  public void doValidation(Object input) throws LinkisClientRuntimeException {
    if (!(input instanceof LinkisSubmitJob)) {
      throw new ValidateException(
          "VLD0007",
          ErrorLevel.ERROR,
          CommonErrMsg.ValidationErr,
          "Input of LinkisSubmitValidator is not instance of LinkisSubmitJob. Type: "
              + input.getClass().getCanonicalName());
    }
    boolean ok = true;
    StringBuilder reasonSb = new StringBuilder();
    LinkisSubmitDesc submitDesc = ((LinkisSubmitJob) input).getJobDesc();
    if (StringUtils.isBlank(submitDesc.getSubmitUser())) {
      reasonSb.append("Submit User cannot be empty or blank").append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank(submitDesc.getProxyUser())) {
      reasonSb
          .append("proxy(execute) User cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getLabelMap() == null) {
      reasonSb.append("labelMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getExecutionMap() == null) {
      reasonSb.append("ExecutionMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getSourceMap() == null) {
      reasonSb.append("SourceMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getParamConfMap() == null) {
      reasonSb.append("startupMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getParamVarsMap() == null) {
      reasonSb.append("variableMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    if (submitDesc.getParamRunTimeMap() == null) {
      reasonSb.append("runTimeMap cannot be null").append(System.lineSeparator());
      ok = false;
    }
    for (Map.Entry<String, Object> entry : submitDesc.getExecutionMap().entrySet()) {
      if (StringUtils.contains(entry.getKey(), " ")) {
        reasonSb
            .append("ExecutionMap key cannot contains space character. key: ")
            .append(entry.getKey())
            .append(System.lineSeparator());
        ok = false;
      }
    }
    for (Map.Entry<String, Object> entry : submitDesc.getLabelMap().entrySet()) {
      if (StringUtils.contains(entry.getKey(), " ")) {
        reasonSb
            .append("LabelMap key cannot contains space character. key: ")
            .append(entry.getKey())
            .append(System.lineSeparator());
        ok = false;
      }
      Object val = entry.getValue();
      if (val instanceof String) {
        if (StringUtils.contains((String) val, " ")) {
          reasonSb
              .append("LabelMap value cannot contains space character. key: ")
              .append(entry.getKey())
              .append("value: ")
              .append(val)
              .append(System.lineSeparator());
          ok = false;
        }
      }
    }
    for (Map.Entry<String, Object> entry : submitDesc.getParamConfMap().entrySet()) {
      if (StringUtils.contains(entry.getKey(), " ")) {
        reasonSb
            .append("startUpMap key cannot contains space character. key: ")
            .append(entry.getKey())
            .append(System.lineSeparator());
        ok = false;
      }
      //            Object val = entry.getValue();
      //            if (val instanceof String) {
      //                if (StringUtils.contains((String) val, " ")) {
      //                    reasonSb.append("startUpMap value cannot contains space character.
      // key: ")
      //                            .append(entry.getKey()).append("value: ").append(val)
      //                            .append(System.lineSeparator());
      //                    ok = false;
      //                }
      //            }
    }
    //        for (Map.Entry<String, Object> entry : linkisJob.getParamRunTimeMap().entrySet())
    // {
    //            if (StringUtils.contains(entry.getKey(), " ")) {
    //                reasonSb.append("runtimeMap key cannot contains space character. key:
    // ").append(entry.getKey()).append(System.lineSeparator());
    //                ok = false;
    //            }
    //            Object val = entry.getValue();
    //            if (val instanceof String) {
    //                if (StringUtils.contains((String) val, " ")) {
    //                    reasonSb.append("runtimeMap value cannot contains space character.
    // key: ")
    //                            .append(entry.getKey()).append("value: ").append(val)
    //                            .append(System.lineSeparator());
    //                    ok = false;
    //                }
    //            }
    //        }
    for (Map.Entry<String, Object> entry : submitDesc.getParamVarsMap().entrySet()) {
      if (StringUtils.contains(entry.getKey(), " ")) {
        reasonSb
            .append("variablesMap key cannot contains space character. key: ")
            .append(entry.getKey())
            .append(System.lineSeparator());
        ok = false;
      }
      Object val = entry.getValue();
      //                if (val instanceof String) {
      //                    if (StringUtils.contains((String) val, " ")) {
      //                        reasonSb.append("variablesMap value cannot contains space
      // character. key: ")
      //                                .append(entry.getKey()).append("value: ").append(val)
      //                                .append(System.lineSeparator());
      //                        ok = false;
      //                    }
      //                }
    }
    for (Map.Entry<String, Object> entry : submitDesc.getSourceMap().entrySet()) {
      if (StringUtils.contains(entry.getKey(), " ")) {
        reasonSb
            .append("sourceMap key cannot contains space character. key: ")
            .append(entry.getKey())
            .append(System.lineSeparator());
        ok = false;
      }
      Object val = entry.getValue();
      if (val instanceof String) {
        if (StringUtils.contains((String) val, " ")) {
          reasonSb
              .append("sourceMap value cannot contains space character. key: ")
              .append(entry.getKey())
              .append("value: ")
              .append(val)
              .append(System.lineSeparator());
          ok = false;
        }
      }
    }
    if (StringUtils.isBlank((String) submitDesc.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE))) {
      reasonSb
          .append(LinkisKeys.KEY_ENGINETYPE)
          .append(" cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank((String) submitDesc.getLabelMap().get(LinkisKeys.KEY_CODETYPE))) {
      reasonSb
          .append(LinkisKeys.KEY_CODETYPE)
          .append(" cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank((String) submitDesc.getSourceMap().get(LinkisKeys.KEY_SCRIPT_PATH))) {
      reasonSb
          .append(LinkisKeys.KEY_SCRIPT_PATH)
          .append(" cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank((String) submitDesc.getExecutionMap().get(LinkisKeys.KEY_CODE))
        && StringUtils.indexOfIgnoreCase(
                (String) submitDesc.getLabelMap().get(LinkisKeys.KEY_ENGINETYPE), "sqoop")
            == -1) {
      reasonSb
          .append(LinkisKeys.KEY_CODE)
          .append(" cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    }
    if (StringUtils.isBlank((String) submitDesc.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR))) {
      reasonSb
          .append(LinkisKeys.KEY_USER_CREATOR)
          .append(" cannot be empty or blank")
          .append(System.lineSeparator());
      ok = false;
    } else {
      String userCreator = (String) submitDesc.getLabelMap().get(LinkisKeys.KEY_USER_CREATOR);
      if (StringUtils.indexOf(submitDesc.getProxyUser(), "-") != -1) {
        reasonSb
            .append("\'proxyUser\' should not contain special character \'-\'")
            .append(System.lineSeparator());
        ok = false;
      } else {
        int idx = StringUtils.indexOf(userCreator, "-");
        if (idx == -1) {
          reasonSb
              .append(LinkisKeys.KEY_USER_CREATOR)
              .append("should contain exactly one character \'-\'")
              .append(System.lineSeparator());
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
            if (StringUtils.containsAny(creator, forBiddenChars)) {
              reasonSb
                  .append("\'creator\' should not contain any special characters except \'_\'")
                  .append(System.lineSeparator());
              ok = false;
            }
          }
        }
      }
    }
    if (!ok) {
      throw new ValidateException(
          "VLD0008",
          ErrorLevel.ERROR,
          CommonErrMsg.ValidationErr,
          "LinkisJob validation failed. Reason: " + reasonSb.toString());
    }
  }
}
