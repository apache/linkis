/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.application.presenter;

import com.webank.wedatasphere.linkis.cli.application.constants.AppConstants;
import com.webank.wedatasphere.linkis.cli.application.presenter.model.LinkisJobIncLogModel;
import com.webank.wedatasphere.linkis.cli.application.utils.Utils;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.PresenterException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.execution.observer.event.TriggerEvent;
import com.webank.wedatasphere.linkis.cli.core.presenter.display.DisplayDriver;
import com.webank.wedatasphere.linkis.cli.core.presenter.display.StdOutDriver;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.JobExecModel;
import com.webank.wedatasphere.linkis.cli.core.presenter.model.PresenterModel;
import com.webank.wedatasphere.linkis.httpclient.dws.response.DWSResult;
import com.webank.wedatasphere.linkis.ujes.client.response.JobLogResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.StringTokenizer;

/**
 * @program: linkis-cli
 * @description:
 * @author: shangda
 * @create: 2021/03/10 10:17
 */
public class LinkisJobLogPresenter extends QueryBasedPresenter {
    private static Logger logger = LoggerFactory.getLogger(LinkisJobLogPresenter.class);

    private TriggerEvent logFinEvent;

//    private Integer cntRealLogLines = 0;//for test

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
//    final DisplayDriver finalDriver = DisplayDriverFactory.getDisplayDriver(finalModel.getOutputWay()); //currently we don't allow printing log to file here
        final DisplayDriver finalDriver = new StdOutDriver();

        Thread incLogPresenterThread = new Thread(() -> {
            LinkisJobIncLogModel incLogModel = finalModel;
            DisplayDriver outDriver = finalDriver;

                Integer oldLogIdx = 0;
                Integer newLogIdx = 0;
                while (!(incLogModel.isJobCompleted() && oldLogIdx == newLogIdx)) {
                    DWSResult jobInfoResult = clientDriver.queryJobInfo(incLogModel.getUser(), incLogModel.getTaskID());
                    incLogModel = updateModelByDwsResult(incLogModel, jobInfoResult);

                    oldLogIdx = incLogModel.getFromLine();
                    try {
                        incLogModel = retrieveRuntimeIncLog(incLogModel);
                    } catch (Exception e) {
                        logger.error("Cannot get runtime-log:", e);
                        incLogModel.readAndClearIncLog();
                        incLogModel.setFromLine(oldLogIdx);
                        if (incLogModel.isJobCompleted()) {
                            break;
                        } else {
                            continue;
                        }
                    }
                    newLogIdx = incLogModel.getFromLine();
                    if (oldLogIdx == newLogIdx) {
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
//            System.out.println("================");
//            System.out.println("previous fromline: " + model.getFromLine());
//            System.out.println("current fromline: " + ((JobLogResult)result).getFromLine());
//            cntRealLogLines += getNumOfLines(((JobLogResult)result).getLog().get(3));
//            System.out.println("actual number of total Log lines: " + cntRealLogLines);
//            System.out.println("number of incLog lines indicated by server: " + (((JobLogResult)result).getFromLine()-model.getFromLine()));
//            System.out.println("actual number of incLog lines: " + getNumOfLines(((JobLogResult)result).getLog().get(3)));
//            System.out.println("................");
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
//        System.out.println("================");
//        System.out.println("number of persisted log lines server: " + getNumOfLines(allLog));
//        System.out.println("fromLine: " + fromLine);
//        System.out.println("actual number of all runtimeLogs:" + cntRealLogLines);
//        System.out.println("................");
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

    protected void doOutput() {

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
        for( int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if( c == '\r' ) {
                lines++;
                if (pos + 1 < len && str.charAt(pos + 1) == '\n') {
                    pos++;
                }
            }else if( c == '\n' ) {
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
        for(int pos = 0; pos < len; pos++) {
            char c = str.charAt(pos);
            if( c == '\r' ) {
                curLineIdx++;
                if ( pos+1 < len && str.charAt(pos+1) == '\n' ) {
                    pos++;
                }
            } else if( c == '\n' ) {
                curLineIdx++;
            } else {
                continue;
            }

            if (curLineIdx >= lines) {
                return pos+1;
            }
        }
        return -1;
    }
}