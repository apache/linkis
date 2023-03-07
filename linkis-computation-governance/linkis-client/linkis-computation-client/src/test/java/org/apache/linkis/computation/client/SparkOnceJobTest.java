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

package org.apache.linkis.computation.client;

import org.apache.linkis.computation.client.once.simple.SubmittableSimpleOnceJob;
import org.apache.linkis.computation.client.operator.impl.*;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkOnceJobTest {

  private static final Logger logger = LoggerFactory.getLogger(SparkOnceJobTest.class);

  public static void main(String[] args) throws InterruptedException {

    LinkisJobClient.config().setDefaultServerUrl("http://127.0.0.1:9001");

    String submitUser = "linkis";
    String engineType = "spark";

    SubmittableSimpleOnceJob onceJob =
        // region
        LinkisJobClient.once()
            .simple()
            .builder()
            .setCreateService("Spark-Test")
            .setMaxSubmitTime(300000)
            .setDescription("SparkTestDescription")
            .addExecuteUser(submitUser)
            .addJobContent("runType", "jar")
            .addJobContent("spark.app.main.class", "org.apache.spark.examples.JavaWordCount")
            .addJobContent("spark.app.args", "hdfs:///tmp/log.log -a 10 -b=12")
            .addJobContent(
                "spark.extconf", "spark.a=d\nspark.c=d\nspark.args.start_date=2022-06-14")
            .addLabel("engineType", engineType + "-2.4.7")
            .addLabel("userCreator", submitUser + "-IDE")
            .addLabel("engineConnMode", "once")
            .addStartupParam("spark.app.name", "spark-submit-jar-test-xi")
            .addStartupParam("spark.executor.memory", "1g")
            .addStartupParam("spark.driver.memory", "1g")
            .addStartupParam("spark.executor.cores", "1")
            .addStartupParam("spark.executor.instance", "1")
            .addStartupParam(
                "spark.app.resource", "hdfs:///spark/spark-examples_2.11-2.3.0.2.6.5.0-292.jar")
            .addSource("jobName", "OnceJobTest")
            .build();
    // endregion
    onceJob.submit();
    logger.info("jobId: {}", onceJob.getId());

    EngineConnLogOperator logOperator =
        (EngineConnLogOperator) onceJob.getOperator(EngineConnLogOperator.OPERATOR_NAME());
    int fromLine = 1;
    int logSize = 1000; // log lines
    logOperator.setPageSize(logSize);
    logOperator.setEngineConnType("spark");

    ArrayList<String> logLines;

    // print log
    while (true) {
      try {
        logOperator.setFromLine(fromLine);
        EngineConnLogs logs = (EngineConnLogs) logOperator.apply();
        logLines = logs.logs();
        if (logLines == null || logLines.isEmpty()) {
          if (!isCompleted(onceJob, 3)) {
            Thread.sleep(2000);
            continue;
          } else {
            break;
          }
        }
        for (String log : logLines) {
          System.out.println(log);
        }
        fromLine += logLines.size();
      } catch (Exception e) {
        logger.error("Failed to get log information", e);
        break;
      }
    }

    boolean complete = false;
    // wait complete
    while (!complete) {
      try {
        complete = onceJob.isCompleted();
      } catch (Exception e) {
        logger.error("isCompleted error", e);
      }
    }

    String finalStatus = onceJob.getStatus();
    logger.info("final status " + finalStatus);
  }

  static boolean isCompleted(SubmittableSimpleOnceJob onceJob, int times)
      throws InterruptedException {
    if (times == 0) return false;
    try {
      return onceJob.isCompleted();
    } catch (Exception e) {
      logger.error("isCompleted error", e);
      Thread.sleep(2000);
      return isCompleted(onceJob, times - 1);
    }
  }
}
