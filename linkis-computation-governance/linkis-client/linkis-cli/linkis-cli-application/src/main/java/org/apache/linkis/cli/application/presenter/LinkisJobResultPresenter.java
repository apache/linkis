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
 
package org.apache.linkis.cli.application.presenter;

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.presenter.model.LinkisJobResultModel;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.entity.job.OutputWay;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.presenter.display.DisplayDriver;
import org.apache.linkis.cli.core.presenter.display.StdOutDriver;
import org.apache.linkis.cli.core.presenter.display.data.FileOutData;
import org.apache.linkis.cli.core.presenter.display.factory.DisplayDriverFactory;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * @description: Display Result of Linkis task
 */
public class LinkisJobResultPresenter extends QueryBasedPresenter {
    private static Logger logger = LoggerFactory.getLogger(LinkisJobResultPresenter.class);

    @Override
    public void present(PresenterModel model) {
        checkInit();
        if (!(model instanceof LinkisJobResultModel)) {
            throw new PresenterException("PST0001", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Input model for \"LinkisJobResultPresenter\" is not instance of \"LinkisJobResultModel\"");
        }

        if (!((LinkisJobResultModel) model).isJobCompleted()) {
            throw new PresenterException("PST0011", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Job is not completed but triggered ResultPresenter");
        }

        new StdOutDriver().doOutput(formatResultIndicator((LinkisJobResultModel) model));

        if (!((LinkisJobResultModel) model).isJobSuccess()) {
            logger.info("Job status is not success but \'" + ((LinkisJobResultModel) model).getJobStatus() + "\'. Will not try to retrieve any Result");
            return;
        }

        LinkisJobResultModel resultModel = (LinkisJobResultModel) model;
        DisplayDriver displayDriver = getDriver(resultModel.getOutputWay());
        String outputPath = resultModel.getOutputPath();
        String[] resultSetPaths = resultModel.getResultSetPaths();
        StringBuilder resultSb = new StringBuilder();
        if (resultSetPaths == null) {
            Exception e = new PresenterException("PST0012", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "ResultPresenter got null as ResultSet");
            logger.warn("", e);
            return;
        }

        for (int i = 0; i < resultSetPaths.length; i++) {
            String resultSetPath = resultSetPaths[i];
            Integer curPage = 1;
            List<LinkedHashMap<String, String>> metaData = null;
//      while (curPage < resultModel.getTotalPage()) { //NOT WORKING
            while (true) {
                resultSb.setLength(0);
                DWSResult result;
                try {
                    result = clientDriver.queryResultSetGivenResultSetPath(resultSetPath, resultModel.getUser(), curPage, AppConstants.RESULTSET_PAGE_SIZE);
                } catch (Exception e) {
                    logger.warn("Cannot retrieve resetset from path: " + resultSetPath, e);
                    continue;
                }
                resultModel = (LinkisJobResultModel) transformer.convertAndUpdateModel(resultModel, result);
                if (curPage == 1) {
                    try {
                        metaData = transformer.convertResultMeta(resultModel.getResultMetaData());
                        resultSb.append(formatResultMeta(metaData)).append(System.lineSeparator());
                    } catch (Exception e) {
                        logger.warn("Cannot convert ResultSet-Meta-Data. ResultSet-Meta-Data:" + Utils.GSON.toJson(resultModel.getResultMetaData()), e);
                        continue;
                    }
                }
                List<List<String>> contentData;
                try {
                    contentData = transformer.convertResultContent(resultModel.getResultContent());
                } catch (Exception e) {
                    logger.warn("Cannot convert ResultSet-Content. ResultSet-Content:" + Utils.GSON.toJson(resultModel.getResultContent()), e);
                    continue;
                }
                int idx = i + 1;
                String fileName = resultModel.getUser() + "-task-" + resultModel.getJobID() + "-result-" + idx + ".txt";
                if (curPage == 1) {
                    String contentStr = formatResultContent(false, metaData, contentData);
                    resultSb.append(contentStr);
                    displayDriver.doOutput(new FileOutData(outputPath, fileName, resultSb.toString(), true));
                } else {
                    String contentStr = formatResultContent(false, metaData, contentData);
                    resultSb.append(contentStr);
                    displayDriver.doOutput(new FileOutData(outputPath, fileName, resultSb.toString(), false));
                }
                if (contentData == null || contentData.size() == 0) {
                    break;
                }
                curPage++;
            }
        }
        if (((LinkisJobResultModel) model).getOutputWay() == OutputWay.TEXT_FILE ||
                StringUtils.isNotBlank(outputPath)) {
            LogUtils.getInformationLogger().info("ResultSet has been successfully written to path: " +
                    outputPath);
        }
    }

    protected DisplayDriver getDriver(OutputWay outputWay) {
        return DisplayDriverFactory.getDisplayDriver(outputWay);
    }

    protected String formatResultIndicator(LinkisJobResultModel model) {
        StringBuilder infoBuilder = new StringBuilder();
        String extraMsgStr = "";

        if (model.getExtraMsg() != null) {
            extraMsgStr = model.getExtraMsg().toString();
        }
        if (model.isJobSuccess()) {

            LogUtils.getInformationLogger().info("Job execute successfully! Will try get execute result");
            infoBuilder.append("============Result:================").append(System.lineSeparator())
                    .append("TaskId:").append(model.getTaskID()).append(System.lineSeparator())
                    .append("ExecId: ").append(model.getExecID()).append(System.lineSeparator())
                    .append("User:").append(model.getUser()).append(System.lineSeparator())
                    .append("Current job status:").append(model.getJobStatus()).append(System.lineSeparator())
                    .append("extraMsg: ").append(extraMsgStr).append(System.lineSeparator())
                    .append("result: ").append(extraMsgStr).append(System.lineSeparator());
        } else if (model.isJobCompleted()) {
            LogUtils.getInformationLogger().info("Job failed! Will not try get execute result.");
            infoBuilder.append("============Result:================").append(System.lineSeparator())
                    .append("TaskId:").append(model.getTaskID()).append(System.lineSeparator())
                    .append("ExecId: ").append(model.getExecID()).append(System.lineSeparator())
                    .append("User:").append(model.getUser()).append(System.lineSeparator())
                    .append("Current job status:").append(model.getJobStatus()).append(System.lineSeparator())
                    .append("extraMsg: ").append(extraMsgStr).append(System.lineSeparator());
            if (model.getErrCode() != null) {
                infoBuilder.append("errCode: ").append(model.getErrCode()).append(System.lineSeparator());
            }
            if (StringUtils.isNotBlank(model.getErrDesc())) {
                infoBuilder.append("errDesc: ").append(model.getErrDesc()).append(System.lineSeparator());
            }
        } else {
            throw new PresenterException("PST0011", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Job is not completed but triggered ResultPresenter");
        }
        return infoBuilder.toString();
    }

    protected String formatResultMeta(List<LinkedHashMap<String, String>> metaData) {

        StringBuilder outputBuilder = new StringBuilder();

        if (metaData == null || metaData.size() == 0) {
            return outputBuilder.toString();
        }
        outputBuilder.append(AppConstants.RESULTSET_META_BEGIN_LOGO).append(System.lineSeparator());

        List<String> titles = new ArrayList<>();

        //gather keys as title
        for (LinkedHashMap<String, String> mapElement : metaData) {
            if (mapElement == null || mapElement.size() == 0) {
                continue;
            }

            Set<Map.Entry<String, String>> entrySet = mapElement.entrySet();
            if (entrySet == null) {
                break;
            }
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                if (key != null && !titles.contains(key)) {
                    titles.add(key);
                    outputBuilder.append(key).append("\t");
                }
            }

        }

        outputBuilder.append(System.lineSeparator());

        //gather value and print to output
        for (LinkedHashMap<String, String> mapElement : metaData) {
            if (mapElement == null || mapElement.size() == 0) {
                continue;
            }
            String candidate;
            for (String title : titles) {
                if (mapElement.containsKey(title)) {
                    candidate = mapElement.get(title);
                } else {
                    candidate = "NULL";
                }
                outputBuilder.append(candidate).append("\t");
            }
            outputBuilder.append(System.lineSeparator());
        }
        outputBuilder.append(AppConstants.RESULTSET_META_END_LOGO).append(System.lineSeparator());
        return outputBuilder.toString();
    }

    protected String formatResultContent(Boolean showMetaInfo, List<LinkedHashMap<String, String>> metaData, List<List<String>> contentData) {

        StringBuilder outputBuilder = new StringBuilder();
        if (contentData == null || contentData.size() == 0) { //finished
            return outputBuilder.append(AppConstants.RESULTSET_END_LOGO).append(System.lineSeparator()).toString();
        }

        outputBuilder.append(AppConstants.RESULTSET_BEGIN_LOGO).append(System.lineSeparator());
        if (metaData != null && showMetaInfo) {
            for (LinkedHashMap<String, String> mapElement : metaData) {
                if (mapElement == null || mapElement.size() == 0) {
                    continue;
                }
                Set<Map.Entry<String, String>> entrySet = mapElement.entrySet();
                if (entrySet == null) {
                    break;
                }
                outputBuilder.append(mapElement.entrySet().iterator().next().getValue()).append("\t");
            }
            outputBuilder.append(System.lineSeparator());
            outputBuilder.append(AppConstants.RESULTSET_SEPARATOR_LOGO);
            outputBuilder.append(System.lineSeparator());
        }

        int listLen = contentData.size();
        for (int i = 0; i < listLen; i++) {
            List<String> listElement = contentData.get(i);
            if (listElement == null || listElement.size() == 0) {
                continue;
            }
            for (String element : listElement) {
                outputBuilder.append(element).append("\t");
            }
            if (i < listLen - 1) {
                outputBuilder.append(System.lineSeparator());
            }
        }

        return outputBuilder.toString();
    }
}