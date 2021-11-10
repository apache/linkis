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
import org.apache.linkis.cli.application.presenter.model.LinkisJobIncLogModel;
import org.apache.linkis.cli.application.utils.Utils;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.execution.observer.event.TriggerEvent;
import org.apache.linkis.cli.core.presenter.display.DisplayDriver;
import org.apache.linkis.cli.core.presenter.display.StdOutDriver;
import org.apache.linkis.cli.core.presenter.model.JobExecModel;
import org.apache.linkis.cli.core.presenter.model.PresenterModel;
import org.apache.linkis.httpclient.dws.response.DWSResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * @description: Display Log while submitting Job. Triggered by {@link TriggerEvent}
 */
public class LinkisJobLogPresenter extends QueryBasedPresenter {
    private static Logger logger = LoggerFactory.getLogger(LinkisJobLogPresenter.class);

    private TriggerEvent logFinEvent;


    public void setLogFinEvent(TriggerEvent logFinEvent) {
        this.logFinEvent = logFinEvent;
    }

    @Override
    public void present(PresenterModel model) {
        checkInit();
        if (!(model instanceof LinkisJobIncLogModel)) {
            throw new PresenterException("PST0001", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Input model for \"LinkisJobLogPresenter\" is not instance of \"LinkisJobIncLogModel\"");

        }
        final LinkisJobIncLogModel finalModel = (LinkisJobIncLogModel) model;
        final DisplayDriver finalDriver = new StdOutDriver();

        Thread incLogPresenterThread = new Thread(() -> {
            LinkisJobIncLogModel incLogModel = finalModel;
            DisplayDriver outDriver = finalDriver;

            Integer oldLogIdx = 0;
            Integer newLogIdx = 0;
            int retryCnt = 0;
            final int MAX_RETRY = 30; // continues fails for 300s, then exit thread
            while (!(incLogModel.isJobCompleted() && oldLogIdx.equals(newLogIdx))) {
                try {
                    DWSResult jobInfoResult = clientDriver.queryJobInfo(incLogModel.getUser(), incLogModel.getTaskID());
                    incLogModel = updateModelByDwsResult(incLogModel, jobInfoResult);
                    oldLogIdx = incLogModel.getFromLine();
                    incLogModel = retrieveRuntimeIncLog(incLogModel);
                } catch (Exception e) {
                    logger.error("Cannot get runtime-log:", e);
                    incLogModel.readAndClearIncLog();
                    incLogModel.setFromLine(oldLogIdx);
                    retryCnt++;
                    if (retryCnt >= MAX_RETRY) {
                        logger.error("Continuously failing to query inc-log for " + MAX_RETRY * 5 * AppConstants.JOB_QUERY_SLEEP_MILLS + "s. Will no longer try to query log", e);
                        return;
                    }
                    Utils.doSleepQuietly(5 * AppConstants.JOB_QUERY_SLEEP_MILLS); //maybe server problem. sleep longer
                    continue;
                }
                retryCnt = 0;//reset counter
                newLogIdx = incLogModel.getFromLine();
                if (oldLogIdx.equals(newLogIdx)) {
                    String msg = MessageFormat.format("Job is still running, status={0}, progress={1}",
                            incLogModel.getJobStatus(),
                            String.valueOf(incLogModel.getJobProgress() * 100) + "%");
                    logger.info(msg);
                } else {
                    String incLog = incLogModel.readAndClearIncLog();
                    outDriver.doOutput(incLog);
                }
                Utils.doSleepQuietly(AppConstants.JOB_QUERY_SLEEP_MILLS);
            }
            try {
                incLogModel = retrievePersistedIncLog(incLogModel);
                String incLog = incLogModel.readAndClearIncLog();
                outDriver.doOutput(incLog);
            } catch (Exception e) {
                logger.error("Cannot get persisted-log: ", e);
            }

            logFinEvent.notifyObserver(logFinEvent, "");

        }, "Inc-Log-Presenter");

        incLogPresenterThread.start();


    }

    protected LinkisJobIncLogModel retrieveRuntimeIncLog(LinkisJobIncLogModel model) {
        //get inclog
        try {
            DWSResult result = clientDriver.queryRunTimeLogFromLine(model.getUser(), model.getTaskID(), model.getExecID(), model.getFromLine());
            model = updateModelByDwsResult(model, result);
        } catch (Exception e) {
            // job is finished while we start query log(but request is not send).
            // then probably server cache is gone and we got a exception here.
            // catch it and use openLog api to retrieve log

            logger.warn("Caught exception when querying runtime-log. Probably server-side has close stream. Will try openLog api if Job is completed.", e);
            throw e;
        }
        return model;
    }

    protected LinkisJobIncLogModel retrievePersistedIncLog(LinkisJobIncLogModel model) {
        DWSResult result = clientDriver.queryPersistedLogAll(model.getLogPath(), model.getUser(), model.getTaskID());
        model = updateModelByDwsResult(model, result);
        String allLog = model.readAndClearIncLog();

        int fromLine = model.getFromLine();
        int idx = getFirstIndexSkippingLines(allLog, fromLine);
        if (idx != -1) {
            String incLog = StringUtils.substring(allLog, idx);
            fromLine = getNumOfLines(incLog) + fromLine;
            model.setFromLine(fromLine);
            if (!incLog.equals("")) {
                model.writeIncLog(incLog);
            }
        }
        return model;
    }


    private LinkisJobIncLogModel updateModelByDwsResult(LinkisJobIncLogModel model, DWSResult result) {
        JobExecModel data = transformer.convertAndUpdateModel(model, result);
        if (!(data instanceof LinkisJobIncLogModel)) {
            throw new PresenterException("PST0004", ErrorLevel.ERROR, CommonErrMsg.PresenterErr, "Error converting \"DWSResult\" into \"LinkisJobIncLogModel\": conversion result is not instance of \"LinkisJobIncLogModel\"");

        }
        return (LinkisJobIncLogModel) data;
    }

    private int getNumOfLines(String str) {
        if (str == null || str.length() == 0) {
            return 0;
        }
        int lines = 1;
        int len = str.length();
        for (int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if (c == '\r') {
                lines++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            } else if (c == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private int getFirstIndexSkippingLines(String str, Integer lines) {
        if (str == null || str.length() == 0 || lines < 0) {
            return -1;
        }
        if (lines == 0) {
            return 0;
        }

        int curLineIdx = 0;
        int len = str.length();
        for (int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if (c == '\r') {
                curLineIdx++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            } else if (c == '\n') {
                curLineIdx++;
            } else {
                continue;
            }

            if (curLineIdx >= lines) {
                return pos + 1;
            }
        }
        return -1;
    }
}