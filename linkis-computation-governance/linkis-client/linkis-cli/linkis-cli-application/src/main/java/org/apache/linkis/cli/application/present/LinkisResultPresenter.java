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

import org.apache.linkis.cli.application.constants.AppConstants;
import org.apache.linkis.cli.application.interactor.job.data.LinkisResultSet;
import org.apache.linkis.cli.application.present.model.LinkisResultModel;
import org.apache.linkis.cli.common.entity.present.Model;
import org.apache.linkis.cli.common.entity.present.PresentWay;
import org.apache.linkis.cli.common.entity.present.Presenter;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PresenterException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.present.PresentModeImpl;
import org.apache.linkis.cli.core.present.PresentWayImpl;
import org.apache.linkis.cli.core.present.display.DisplayOperFactory;
import org.apache.linkis.cli.core.present.display.DisplayOperator;
import org.apache.linkis.cli.core.present.display.data.FileDisplayData;
import org.apache.linkis.cli.core.utils.CommonUtils;
import org.apache.linkis.cli.core.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

public class LinkisResultPresenter implements Presenter {

  @Override
  public void present(Model model, PresentWay presentWay) {
    if (!(model instanceof LinkisResultModel)) {
      throw new PresenterException(
          "PST0001",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Input model for \"LinkisResultPresenter\" is not instance of \"LinkisResultModel\"");
    }
    if (!(presentWay instanceof PresentWayImpl)) {
      throw new PresenterException(
          "PST0002",
          ErrorLevel.ERROR,
          CommonErrMsg.PresenterErr,
          "Input PresentWay for \"LinkisResultPresenter\" is not instance of \"PresentWayImpl\"");
    }
    LinkisResultModel resultModel = (LinkisResultModel) model;
    PresentWayImpl presentWay1 = (PresentWayImpl) presentWay;

    if (!resultModel.getJobStatus().isJobSuccess()) {
      LogUtils.getInformationLogger()
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
    LogUtils.getInformationLogger().info(msg);

    final DisplayOperator displayOperator =
        DisplayOperFactory.getDisplayOper(
            presentWay1.getMode()); // currently we don't allow printing log to file here

    int preIdx = -1;
    StringBuilder resultSb = new StringBuilder();

    while (!resultModel.resultFinReceived()) {
      preIdx = presentOneIteration(resultModel, preIdx, presentWay1, resultSb, displayOperator);
      CommonUtils.doSleepQuietly(500l);
    }
    presentOneIteration(resultModel, preIdx, presentWay1, resultSb, displayOperator);

    if (presentWay1.getMode() == PresentModeImpl.TEXT_FILE
        || StringUtils.isNotBlank(presentWay1.getPath())) {
      LogUtils.getInformationLogger()
          .info("ResultSet has been successfully written to path: " + presentWay1.getPath());
    }
  }

  protected int presentOneIteration(
      LinkisResultModel resultModel,
      int preIdx,
      PresentWayImpl presentWay,
      StringBuilder resultSb,
      DisplayOperator displayOperator) {
    List<LinkisResultSet> linkisResultSets = resultModel.consumeResultContent();
    if (linkisResultSets != null && !linkisResultSets.isEmpty()) {
      for (LinkisResultSet c : linkisResultSets) {
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
        if (presentWay.isDisplayMetaAndLogo()) {
          if (idxResultset - preIdx == 1) {
            resultSb.setLength(0);
            resultSb
                .append(MessageFormat.format(AppConstants.RESULTSET_LOGO, idxResultset + 1))
                .append(System.lineSeparator());
            if (c.getResultMeta() != null) {
              resultSb
                  .append(AppConstants.RESULTSET_META_BEGIN_LOGO)
                  .append(System.lineSeparator());
              resultSb.append(formatResultMeta(c.getResultMeta()));
              resultSb.append(AppConstants.RESULTSET_META_END_LOGO).append(System.lineSeparator());
            }
          }
        }
        preIdx = idxResultset;
        String contentStr = formatResultContent(c.getResultMeta(), c.getContent());
        if (contentStr != null) {
          resultSb.append(contentStr);
        }
        if (resultSb.length() != 0) {
          String resultFileName =
              resultModel.getUser()
                  + "-task-"
                  + resultModel.getJobID()
                  + "-result-"
                  + String.valueOf(idxResultset + 1)
                  + ".txt";
          displayOperator.doOutput(
              new FileDisplayData(presentWay.getPath(), resultFileName, resultSb.toString(), flag));
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
}
