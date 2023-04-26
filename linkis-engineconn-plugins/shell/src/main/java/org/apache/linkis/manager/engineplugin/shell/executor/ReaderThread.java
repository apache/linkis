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

package org.apache.linkis.manager.engineplugin.shell.executor;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.engineconn.computation.executor.execute.EngineExecutionContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReaderThread extends Thread {
  private static final Logger logger = LoggerFactory.getLogger(ReaderThread.class);

  private EngineExecutionContext engineExecutionContext;
  private BufferedReader inputReader;
  private YarnAppIdExtractor extractor;
  private boolean isStdout;
  private final int logListCount =
      CommonVars.apply("wds.linkis.engineconn.log.list.count", 50).getValue();
  private CountDownLatch counter;

  private boolean isReaderAlive = true;

  public ReaderThread(
      EngineExecutionContext engineExecutionContext,
      BufferedReader inputReader,
      YarnAppIdExtractor extractor,
      boolean isStdout,
      CountDownLatch counter) {
    this.engineExecutionContext = engineExecutionContext;
    this.inputReader = inputReader;
    this.extractor = extractor;
    this.isStdout = isStdout;
    this.counter = counter;
  }

  public void onDestroy() {
    isReaderAlive = false;
  }

  @Override
  public void run() {
    String line = null;
    List<String> logArray = new ArrayList<>();
    while (true) {
      try {
        line = inputReader.readLine();
        if (!(line != null && isReaderAlive)) break;
      } catch (IOException e) {
        logger.warn("inputReader reading the input stream");
        break;
      }
      logger.info("read logger line :{}", line);
      logArray.add(line);
      extractor.appendLineToExtractor(line);
      if (isStdout) {
        engineExecutionContext.appendTextResultSet(line);
      }
      if (logArray.size() > logListCount) {
        String linelist = StringUtils.join(logArray, "\n");
        engineExecutionContext.appendStdout(linelist);
        logArray.clear();
      }
    }
    if (logArray.size() > 0) {
      String linelist = StringUtils.join(logArray, "\n");
      engineExecutionContext.appendStdout(linelist);
      logArray.clear();
    }
    IOUtils.closeQuietly(inputReader);
    counter.countDown();
  }
}
