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

package org.apache.linkis.engineplugin.repl.executor;

import org.apache.linkis.engineplugin.repl.errorcode.ReplErrorCodeSummary;
import org.apache.linkis.engineplugin.repl.exception.ReplException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

import scala.tools.nsc.Settings;
import scala.tools.nsc.interpreter.ILoop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScalaReplAdapter extends ReplAdapter {

  private static final Logger logger = LoggerFactory.getLogger(ScalaReplAdapter.class);

  @Override
  public void executorCode(String code, String classpathDir, String methodName) {
    StringReader stringReader = new StringReader(code);
    StringWriter stringWriter = new StringWriter();

    ILoop repl = new ILoop(new BufferedReader(stringReader), new PrintWriter(stringWriter));

    try {
      logger.info("Scala repl start executor");

      Settings settings = new Settings();
      settings.usejavacp().tryToSetFromPropertyValue("true");

      if (StringUtils.isNotBlank(classpathDir) && FileUtils.isDirectory(new File(classpathDir))) {
        settings.classpath().value_$eq(classpathDir);
      }

      boolean process = repl.process(settings);

      String scalaReplLog = stringWriter.toString();
      logger.info("Scala repl log: {}", scalaReplLog);

      if (process) {
        logger.info("Scala repl executor success");
      } else {
        logger.error("Scala repl executor failed");
        throw new ReplException(
            ReplErrorCodeSummary.REPL_SCALA_TASK_EXECUTOR_FAILED.getErrorCode(),
            ReplErrorCodeSummary.REPL_SCALA_TASK_EXECUTOR_FAILED.getErrorDesc());
      }

    } finally {
      repl.closeInterpreter();
    }
  }
}
