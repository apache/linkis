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
import org.apache.linkis.ujes.client.exception.UJESJobException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.function.Supplier;

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
            .addLabel("engineType", engineType + "-2.4.3")
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

    // test
    // Kill after 10s
    // new Thread(() -> {
    //     try {
    //         Thread.sleep(10000);
    //     } catch (InterruptedException e) {
    //         throw new RuntimeException(e);
    //     }
    //     onceJob.kill();
    // }).start();

    // method 1：wait for completing
    waitComplete(onceJob);

    // method 2：wait for completing and print log
    // waitForCompletedAndPrintLog(onceJob);
  }

  static void printProgress(SubmittableSimpleOnceJob onceJob) {
    // get progress
    EngineConnProgressOperator progressOperator =
        (EngineConnProgressOperator)
            onceJob.getOperator(EngineConnProgressOperator.OPERATOR_NAME());
    new Thread(
            () -> {
              while (true) {
                try {
                  EngineConnProgressInfo engineConnProgressInfo2 =
                      (EngineConnProgressInfo) progressOperator.apply();
                  System.out.println("progress: " + engineConnProgressInfo2.progress());
                  if (engineConnProgressInfo2.progress() >= 1) break;
                } catch (Exception e) {
                  System.out.println("progress: error");
                }
                try {
                  Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
              }
            })
        .start();
  }

  static void waitComplete(SubmittableSimpleOnceJob onceJob) throws InterruptedException {

    // Wait for the end of the operation, and there may be exceptions, which may cause client
    // interruption
    boolean finished = false;
    int retryTimes = 10;
    boolean killed = false;
    while (!finished) {
      try {
        onceJob.waitForCompleted();
        finished = true;
      } catch (Exception exception) {
        if (exception instanceof UJESJobException) {
          UJESJobException ujesJobException = (UJESJobException) exception;
          if (ujesJobException.getDesc().endsWith("is killed!")) {
            killed = true;
            break;
          }
        }
        if (--retryTimes == 0) {
          break;
        }
      }
    }

    if (killed) {
      System.out.println("Job has been killed");
      return;
    }

    System.out.println(LocalDateTime.now() + "get complete: job status " + onceJob.getStatus());
    boolean complete = onceJob.isCompleted(onceJob.getStatus());
    // Judge and wait for the final completion.
    // todo Here you may need to set the number of times to handle other exceptions
    int i = 0;
    // If the acquisition fails for 10 times, the connection may be lost
    while (!complete && i++ < 10) {
      try {
        complete = onceJob.isCompleted();
      } catch (Exception e) {
        logger.error("Get isCompleted error, retry after 2s");
        Thread.sleep(2000);
      }
    }
  }

  static void waitForCompletedAndPrintLog(SubmittableSimpleOnceJob onceJob)
      throws InterruptedException {

    EngineConnLogOperator logOperator =
        (EngineConnLogOperator) onceJob.getOperator(EngineConnLogOperator.OPERATOR_NAME());
    int fromLine = 1;
    int logSize = 1000; // log lines
    logOperator.setPageSize(logSize);
    logOperator.setEngineConnType("spark_jar");

    ArrayList<String> logLines;
    EngineConnLogs logs;
    int emptyLogsCount = 0;
    // print log
    while (true) {
      // set start line
      logOperator.setFromLine(fromLine);
      Supplier<EngineConnLogs> supplier =
          () -> {
            onceJob.isCompleted();
            return (EngineConnLogs) logOperator.apply();
          };
      logs = retry(supplier, 3, 2000, null, "Get Logs error");
      if (logs == null) {
        break;
      }

      if ((logLines = logs.logs()) == null || logLines.isEmpty()) {
        emptyLogsCount++;
        // The log is empty for 10 times, indicating that the back-end engine may be killed
        if (emptyLogsCount == 10) {
          break;
        }
        // The log is empty and incomplete. Sleep for 2 seconds.
        // isCompleted method may fail due to network errors and other reasons. Retry 3 times first
        boolean completed = retry(onceJob::isCompleted, 3, 2000, true, "isCompleted error");
        if (!completed) {
          Thread.sleep(2000);
          continue;
        } else {
          break;
        }
      }
      emptyLogsCount = 0;
      for (String log : logLines) {
        // print log
        System.out.println(log);
      }
      // set new start line
      fromLine += logLines.size();
    }
    System.out.println(LocalDateTime.now() + "get complete: job status " + onceJob.getStatus());
    boolean complete = onceJob.isCompleted(onceJob.getStatus());
    // Judge and wait for the final completion.
    // todo Here you may need to set the number of times to handle other exceptions
    int i = 0;
    while (!complete && i++ < 10) {
      try {
        complete = onceJob.isCompleted();
      } catch (Exception e) {
        logger.error("Get isCompleted error, retry after 2s");
        Thread.sleep(2000);
      }
    }
    String finalStatus = onceJob.getStatus();
    // If the final status is running, the program may terminate abnormally
    // Can be set as Canceled/Lost/Killed
    if ("Running".equals(finalStatus)) finalStatus = "Cancelled";
    System.out.println(LocalDateTime.now() + "finalStatus, " + finalStatus);
  }

  static <R> R retry(
      Supplier<R> supplier, int times, int retryDuration, R finalDefaultResult, String errMsg)
      throws InterruptedException {
    if (times <= 0) return finalDefaultResult;
    try {
      return supplier.get();
    } catch (Exception e) {
      logger.error(errMsg);
      Thread.sleep(retryDuration);
      return retry(supplier, times - 1, retryDuration, finalDefaultResult, errMsg);
    }
  }
}
