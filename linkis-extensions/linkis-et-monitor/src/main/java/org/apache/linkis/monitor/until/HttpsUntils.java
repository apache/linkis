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

package org.apache.linkis.monitor.until;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.datasource.client.response.GetInfoPublishedByDataSourceNameResult;
import org.apache.linkis.governance.common.conf.GovernanceCommonConf;
import org.apache.linkis.monitor.client.MonitorHTTPClient;
import org.apache.linkis.monitor.config.MonitorConfig;
import org.apache.linkis.monitor.constants.Constants;
import org.apache.linkis.monitor.entity.ClientSingleton;
import org.apache.linkis.monitor.entity.IndexEntity;
import org.apache.linkis.monitor.jobhistory.entity.JobHistory;
import org.apache.linkis.monitor.request.*;
import org.apache.linkis.monitor.response.AnalyzeJobResultAction;
import org.apache.linkis.monitor.response.EntranceTaskResult;
import org.apache.linkis.monitor.response.KeyvalueResult;
import org.apache.linkis.monitor.response.KillJobResultAction;
import org.apache.linkis.protocol.utils.ZuulEntranceUtils;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.ujes.client.response.EmsListResult;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.springframework.util.Assert;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsUntils {
  private static final Logger logger = LoggerFactory.getLogger(HttpsUntils.class);

  public static final String localHost = Utils.getLocalHostname();

  public static Map<String, Object> getEmsResourceList() throws IOException {
    MonitorHTTPClient client = ClientSingleton.getInstance();
    EmsListAction build = EmsListAction.newBuilder().setUser(Constants.ADMIN_USER()).build();
    EmsListResult result = client.list(build);
    return result.getResultMap();
  }

  public static Map<String, Object> getEntranceTask(String url, String user, String Instance)
      throws IOException {
    MonitorHTTPClient client = ClientSingleton.getInstance();
    EntranceTaskAction build =
        EntranceTaskAction.newBuilder().setUser(user).setInstance(Instance).build();
    EntranceTaskResult result = client.entranList(build);
    return result.getResultMap();
  }

  public static void sendIndex(List<IndexEntity> list) throws IOException {
    Map<String, Object> parm = new HashMap<>();
    parm.put("userAuthKey", MonitorConfig.ECM_TASK_USER_AUTHKEY.getValue());
    parm.put("metricDataList", list);
    String json = BDPJettyServerHelper.gson().toJson(parm);

    RequestConfig requestConfig = RequestConfig.DEFAULT;
    StringEntity entity =
        new StringEntity(
            json,
            ContentType.create(
                ContentType.APPLICATION_JSON.getMimeType(), StandardCharsets.UTF_8.toString()));
    entity.setContentEncoding(StandardCharsets.UTF_8.toString());

    HttpPost httpPost = new HttpPost(MonitorConfig.ECM_TASK_IMURL.getValue());
    httpPost.setConfig(requestConfig);
    httpPost.setEntity(entity);

    CloseableHttpClient httpClient = HttpClients.createDefault();
    CloseableHttpResponse execute = httpClient.execute(httpPost);
    String responseStr =
        EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8.toString());
    Map<String, String> map = BDPJettyServerHelper.gson().fromJson(responseStr, Map.class);
    logger.info("send index response :{}", map);
    Assert.isTrue(!"0".equals(map.get("resultCode")), map.get("resultMsg"));
  }

  public static String getJDBCConf(String user, String conf) {
    MonitorHTTPClient client = ClientSingleton.getInstance();
    KeyvalueAction build =
        KeyvalueAction.newBuilder()
            .setVersion("4")
            .setEngineType(Constants.JDBC_ENGINE())
            .setCreator("IDE")
            .setConfigKey(conf)
            .setUser(user)
            .build();
    KeyvalueResult result = client.getConfKeyValue(build);
    Map data = MapUtils.getMap(result.getResultMap(), "data", new HashMap<>());
    ArrayList arrayList = (ArrayList) data.get("configValues");
    if (CollectionUtils.isNotEmpty(arrayList)) {
      String json = BDPJettyServerHelper.gson().toJson(arrayList.get(0));
      Map map = BDPJettyServerHelper.gson().fromJson(json, Map.class);
      return MapUtils.getString(map, "configValue", "");
    } else {
      return "";
    }
  }

  public static Map getDatasourceConf(String user, String datasourceName) {
    MonitorHTTPClient client = ClientSingleton.getInstance();
    DataSourceParamsAction dataSourceParamsAction =
        DataSourceParamsAction.builder()
            .setSystem(Constants.ALERT_SUB_SYSTEM_ID())
            .setDataSourceName(datasourceName)
            .setUser(user)
            .build();
    GetInfoPublishedByDataSourceNameResult result =
        client.getInfoByDataSourceInfo(dataSourceParamsAction);
    Map data = MapUtils.getMap(result.getResultMap(), "data", new HashMap<>());
    Map datasourceInfoMap = MapUtils.getMap(data, "info", new HashMap<>());
    return datasourceInfoMap;
  }

  public static void killJob(JobHistory jobHistory) {
    MonitorHTTPClient client = ClientSingleton.getInstance();
    String[] split = jobHistory.getInstances().split(Constants.SPLIT_DELIMITER());
    String execID =
        ZuulEntranceUtils.generateExecID(
            jobHistory.getJobReqId(),
            GovernanceCommonConf.ENTRANCE_SERVICE_NAME().getValue(),
            split);
    KillJobAction killJobAction =
        KillJobAction.builder()
            .setIdList(Collections.singletonList(execID))
            .setTaskIDList(Collections.singletonList(jobHistory.getId()))
            .setExecID(execID)
            .setUser(jobHistory.getSubmitUser())
            .build();
    KillJobResultAction killJobResultAction = client.killJob(killJobAction);
    Map data = MapUtils.getMap(killJobResultAction.getResultMap(), "data", new HashMap<>());
  }

  public static void analyzeJob(JobHistory jobHistory) {
    MonitorHTTPClient client = ClientSingleton.getInstance();

    AnalyzeJobAction analyzeJobAction =
        AnalyzeJobAction.newBuilder()
            .setTaskID(String.valueOf(jobHistory.getId()))
            .setUser(Constants.ADMIN_USER())
            .build();
    AnalyzeJobResultAction analyzeJobResultAction = client.analyzeJob(analyzeJobAction);
  }
}
