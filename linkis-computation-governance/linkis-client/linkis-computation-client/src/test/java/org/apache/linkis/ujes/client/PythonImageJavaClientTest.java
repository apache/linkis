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

package org.apache.linkis.ujes.client;

import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.httpclient.dws.authentication.StaticAuthenticationStrategy;
import org.apache.linkis.httpclient.dws.config.DWSClientConfig;
import org.apache.linkis.httpclient.dws.config.DWSClientConfigBuilder;
import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.linkis.protocol.constants.TaskConstant;
import org.apache.linkis.ujes.client.request.JobSubmitAction;
import org.apache.linkis.ujes.client.request.ResultSetAction;
import org.apache.linkis.ujes.client.response.JobExecuteResult;
import org.apache.linkis.ujes.client.response.JobInfoResult;
import org.apache.linkis.ujes.client.response.JobProgressResult;
import org.apache.linkis.ujes.client.response.image.ShowImage;

import org.apache.commons.io.IOUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PythonImageJavaClientTest {
  public static void main(String[] args) {

    String user = "hadoop";
    String gatewayIp = "127.0.0.1:9001";
    String password = "hadoop";

    /** python matplotlib */
    String executeCode =
        "import numpy as np\n"
            + "import matplotlib.pyplot as plt\n"
            + "from matplotlib.font_manager import FontProperties\n"
            + "plt.figure(1)\n"
            + "plt.figure(2)\n"
            + "ax1 = plt.subplot(211)\n"
            + "ax2 = plt.subplot(212)\n"
            + "ax1.set_title(u\"helloworld\")\n"
            + "x = np.linspace(0, 3, 100)\n"
            + "for i in range(5):\n"
            + "    plt.figure(1)\n"
            + "    plt.plot(x, np.exp(i*x/3))\n"
            + "    plt.sca(ax1)\n"
            + "    plt.plot(x, np.sin(i*x))\n"
            + "    plt.sca(ax2)\n"
            + "    plt.plot(x, np.cos(i*x))\n"
            + "\n"
            + "show.show_matplotlib(plt)";

    /** pyspark */
    String pysparkExecuteCode =
        "from pyspark.sql import Row\n"
            + "from pyspark.sql import HiveContext\n"
            + "import pyspark\n"
            + "import matplotlib.pyplot as plt\n"
            + "\n"
            + "plt.rcParams[\"figure.figsize\"] = [7.50, 3.50]\n"
            + "plt.rcParams[\"figure.autolayout\"] = True\n"
            + "\n"
            + "sqlContext = HiveContext(sc)\n"
            + "\n"
            + "test_list = [(1, 'John'), (2, 'James'), (3, 'Jack'), (4, 'Joe')]\n"
            + "rdd = sc.parallelize(test_list)\n"
            + "people = rdd.map(lambda x: Row(id=int(x[0]), name=x[1]))\n"
            + "schemaPeople = sqlContext.createDataFrame(people)\n"
            + "sqlContext.registerDataFrameAsTable(schemaPeople, \"my_table\")\n"
            + "\n"
            + "df = sqlContext.sql(\"Select * from my_table\")\n"
            + "df = df.toPandas()\n"
            + "df.set_index('name').plot()\n"
            + "\n"
            + "show_matplotlib(plt)";

    // 1. ClientBuilder，get ClientConfig
    DWSClientConfig clientConfig =
        ((DWSClientConfigBuilder)
                (DWSClientConfigBuilder.newBuilder()
                    .addServerUrl("http://" + gatewayIp)
                    .connectionTimeout(30000)
                    .discoveryEnabled(false)
                    .discoveryFrequency(1, TimeUnit.MINUTES)
                    .loadbalancerEnabled(true)
                    .maxConnectionSize(5)
                    .retryEnabled(false)
                    .readTimeout(30000)
                    .setAuthenticationStrategy(new StaticAuthenticationStrategy())
                    .setAuthTokenKey(user)
                    .setAuthTokenValue(password)))
            .setDWSVersion("v1")
            .build();

    UJESClient client = new UJESClientImpl(clientConfig);

    try {

      System.out.println("user : " + user + ", code : [" + executeCode + "]");
      Map<String, Object> startupMap = new HashMap<String, Object>();

      startupMap.put("wds.linkis.yarnqueue", "q02");

      Map<String, Object> labels = new HashMap<String, Object>();

      labels.put(LabelKeyConstant.ENGINE_TYPE_KEY, "python-python2");
      labels.put(LabelKeyConstant.USER_CREATOR_TYPE_KEY, user + "-IDE");
      labels.put(LabelKeyConstant.CODE_TYPE_KEY, "python");

      Map<String, Object> source = new HashMap<String, Object>();
      source.put(TaskConstant.SCRIPTPATH, "LinkisClient-test");
      JobExecuteResult jobExecuteResult =
          client.submit(
              JobSubmitAction.builder()
                  .addExecuteCode(executeCode)
                  .setStartupParams(startupMap)
                  .setUser(user)
                  .addExecuteUser(user)
                  .setLabels(labels)
                  .setSource(source)
                  .build());
      System.out.println(
          "execId: " + jobExecuteResult.getExecID() + ", taskId: " + jobExecuteResult.taskID());

      JobInfoResult jobInfoResult = client.getJobInfo(jobExecuteResult);
      int sleepTimeMills = 1000;
      while (!jobInfoResult.isCompleted()) {

        JobProgressResult progress = client.progress(jobExecuteResult);
        Utils.sleepQuietly(sleepTimeMills);
        jobInfoResult = client.getJobInfo(jobExecuteResult);
      }

      JobInfoResult jobInfo = client.getJobInfo(jobExecuteResult);

      String resultSet = jobInfo.getResultSetList(client)[0];

      Object fileContents =
          client
              .resultSet(
                  ResultSetAction.builder()
                      .setPath(resultSet)
                      .setUser(jobExecuteResult.getUser())
                      .build())
              .getFileContent();
      System.out.println("fileContents: " + fileContents);

      //  fileContents to image
      ShowImage.showImage(fileContents, 1000, 1000);

    } catch (Exception e) {
      e.printStackTrace();
      IOUtils.closeQuietly(client);
    }
    IOUtils.closeQuietly(client);
  }
}
