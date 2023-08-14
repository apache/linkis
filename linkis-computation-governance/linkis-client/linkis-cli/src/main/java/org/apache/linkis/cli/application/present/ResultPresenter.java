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

package org.apache.linkis.cli.application.present;

import org.apache.linkis.cli.application.constants.CliConstants;
import org.apache.linkis.cli.application.entity.present.Model;
import org.apache.linkis.cli.application.entity.present.Presenter;
import org.apache.linkis.cli.application.exception.PresenterException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.job.common.ResultSet;
import org.apache.linkis.cli.application.observer.event.LinkisClientEvent;
import org.apache.linkis.cli.application.observer.listener.LinkisClientListener;
import org.apache.linkis.cli.application.present.file.ResultFileWriter;
import org.apache.linkis.cli.application.present.model.LinkisResultModel;
import org.apache.linkis.cli.application.utils.CliUtils;
import org.apache.linkis.cli.application.utils.LoggerManager;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultPresenter implements Presenter, LinkisClientListener {
  private static Logger logger = LoggerFactory.getLogger(ResultPresenter.class);
  private Boolean writeToFile = false;
  private String filePath = "";

  public ResultPresenter() {}

  public ResultPresenter(Boolean writeToFile, String filePath) {
    this.writeToFile = writeToFile;
    this.filePath = filePath;
  }

  @Override
  public void update(LinkisClientEvent event, Object msg) {
    Model model = new LinkisResultModel();
    model.buildModel(msg);
    this.present(model);
  }

  @Override
  public void present(Model model) {
    if (!(model instanceof LinkisResultModel)) {
      throw new PresenterException(
          "PST0001",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Input model for \"LinkisResultPresenter\" is not instance of \"LinkisResultModel\"");
    }
    LinkisResultModel resultModel = (LinkisResultModel) model;

    LoggerManager.getPlaintTextLogger().info(formatResultIndicator(resultModel));

    if (!resultModel.getJobStatus().isJobSuccess()) {
      LoggerManager.getInformationLogger()
          .info("JobStatus is not \'success\'. Will not retrieve result-set.");
      return;
    }
    String msg = "";
    if (resultModel.hasResult()) {
      msg =
          "Retrieving result-set, may take time if result-set is large, please do not exit program.";
    } else {
      msg = "Your job has no result.";
    }
    LoggerManager.getInformationLogger().info(msg);

    int preIdx = -1;
    StringBuilder resultSb = new StringBuilder();

    while (!resultModel.resultFinReceived()) {
      preIdx = presentOneIteration(resultModel, preIdx, resultSb);
      CliUtils.doSleepQuietly(500l);
    }
    presentOneIteration(resultModel, preIdx, resultSb);

    if (writeToFile) {
      LoggerManager.getInformationLogger()
          .info("ResultSet has been successfully written to path: " + filePath);
    }
  }

  protected int presentOneIteration(
      LinkisResultModel resultModel, int preIdx, StringBuilder resultSb) {
    List<ResultSet> resultSets = resultModel.consumeResultContent();
    if (resultSets != null && !resultSets.isEmpty()) {
      for (ResultSet c : resultSets) {
        int idxResultset = c.getResultsetIdx();
        /**
         * Notice: we assume result-sets are visited one by one in non-descending order!!! i.e.
         * either idxResultset == preIdx or idxResultset - preIdx == 1 i.e. resultsets[0] ->
         * resultsets[1] -> ...
         */
        if (idxResultset - preIdx != 0 && idxResultset - preIdx != 1) {
          throw new PresenterException(
              "PST0002",
              ErrorLevel.ERROR,
              CommonErrMsg.PresenterErr,
              "Linkis resultsets are visited in descending order or are not visited one-by-one");
        }

        boolean flag = idxResultset > preIdx;
        if (idxResultset - preIdx == 1) {
          resultSb.setLength(0);
          resultSb
              .append(MessageFormat.format(CliConstants.RESULTSET_LOGO, idxResultset + 1))
              .append(System.lineSeparator());
          if (c.getResultMeta() != null) {
            resultSb.append(CliConstants.RESULTSET_META_BEGIN_LOGO).append(System.lineSeparator());
            resultSb.append(formatResultMeta(c.getResultMeta()));
            resultSb.append(CliConstants.RESULTSET_META_END_LOGO).append(System.lineSeparator());
          }
        }
        preIdx = idxResultset;
        String contentStr = formatResultContent(c.getResultMeta(), c.getContent());
        if (contentStr != null) {
          resultSb.append(contentStr);
        }
        if (resultSb.length() != 0) {
          if (writeToFile) {
            String resultFileName =
                resultModel.getUser()
                    + "-task-"
                    + resultModel.getJobID()
                    + "-result-"
                    + String.valueOf(idxResultset + 1)
                    + ".txt";
            ResultFileWriter.writeToFile(filePath, resultFileName, resultSb.toString(), flag);
          } else {
            LoggerManager.getPlaintTextLogger().info(resultSb.toString());
          }
          resultSb.setLength(0);
        }
      }
    }
    return preIdx;
  }

  protected String formatResultMeta(List<LinkedHashMap<String, String>> metaData) {

    StringBuilder outputBuilder = new StringBuilder();

    if (metaData == null || metaData.size() == 0) {
      return null;
    }

    List<String> titles = new ArrayList<>();

    // gather keys as title
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

    // gather value and print to output
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
    return outputBuilder.toString();
  }

  protected String formatResultContent(
      List<LinkedHashMap<String, String>> metaData, List<List<String>> contentData) {

    StringBuilder outputBuilder = new StringBuilder();
    if (contentData == null || contentData.size() == 0) { // finished
      return null;
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

  protected String formatResultIndicator(LinkisResultModel model) {
    StringBuilder infoBuilder = new StringBuilder();
    String extraMsgStr = "";

    if (model.getExtraMessage() != null) {
      extraMsgStr = model.getExtraMessage();
    }
    if (model.getJobStatus().isJobSuccess()) {

      LoggerManager.getInformationLogger()
          .info("Job execute successfully! Will try get execute result");
      infoBuilder
          .append("============Result:================")
          .append(System.lineSeparator())
          .append("TaskId:")
          .append(model.getJobID())
          .append(System.lineSeparator())
          .append("ExecId: ")
          .append(model.getExecID())
          .append(System.lineSeparator())
          .append("User:")
          .append(model.getUser())
          .append(System.lineSeparator())
          .append("Current job status:")
          .append(model.getJobStatus())
          .append(System.lineSeparator())
          .append("extraMsg: ")
          .append(extraMsgStr)
          .append(System.lineSeparator())
          .append("result: ")
          .append(extraMsgStr)
          .append(System.lineSeparator());
    } else if (model.getJobStatus().isJobFinishedState()) {
      LoggerManager.getInformationLogger().info("Job failed! Will not try get execute result.");
      infoBuilder
          .append("============Result:================")
          .append(System.lineSeparator())
          .append("TaskId:")
          .append(model.getJobID())
          .append(System.lineSeparator())
          .append("ExecId: ")
          .append(model.getExecID())
          .append(System.lineSeparator())
          .append("User:")
          .append(model.getUser())
          .append(System.lineSeparator())
          .append("Current job status:")
          .append(model.getJobStatus())
          .append(System.lineSeparator())
          .append("extraMsg: ")
          .append(extraMsgStr)
          .append(System.lineSeparator());
      if (model.getErrCode() != null) {
        infoBuilder.append("errCode: ").append(model.getErrCode()).append(System.lineSeparator());
      }
      if (StringUtils.isNotBlank(model.getErrDesc())) {
        infoBuilder.append("errDesc: ").append(model.getErrDesc()).append(System.lineSeparator());
      }
    } else {
      throw new PresenterException(
          "PST0011",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Job is not completed but triggered ResultPresenter");
    }
    return infoBuilder.toString();
  }
}
