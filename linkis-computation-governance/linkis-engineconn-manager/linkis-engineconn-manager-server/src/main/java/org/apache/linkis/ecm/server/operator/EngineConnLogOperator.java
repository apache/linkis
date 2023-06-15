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

package org.apache.linkis.ecm.server.operator;

import org.apache.linkis.common.conf.CommonVars;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.ecm.server.conf.ECMConfiguration;
import org.apache.linkis.ecm.server.exception.ECMErrorException;
import org.apache.linkis.manager.common.operator.Operator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.ecm.errorcode.EngineconnServerErrorCodeSummary.*;

public class EngineConnLogOperator implements Operator {
  private static final Logger logger = LoggerFactory.getLogger(EngineConnLogOperator.class);

  public static final String OPERATOR_NAME = "engineConnLog";
  public static final CommonVars<String> LOG_FILE_NAME =
      CommonVars.apply("linkis.engineconn.log.filename", "stdout");
  public static final CommonVars<Integer> MAX_LOG_FETCH_SIZE =
      CommonVars.apply("linkis.engineconn.log.fetch.lines.max", 5000);
  public static final CommonVars<Integer> MAX_LOG_TAIL_START_SIZE =
      CommonVars.apply("linkis.engineconn.log.tail.start.size");
  public static final CommonVars<String> MULTILINE_PATTERN =
      CommonVars.apply(
          "linkis.engineconn.log.multiline.pattern",
          "^\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
  public static final CommonVars<Integer> MULTILINE_MAX =
      CommonVars.apply("linkis.engineconn.log.multiline.max", 500);

  @Override
  public String[] getNames() {
    return new String[] {OPERATOR_NAME};
  }

  @Override
  public Map<String, Object> apply(Map<String, Object> parameters) {
    File logPath = getLogPath(parameters);
    int lastRows = getAs(parameters, "lastRows", 0);
    int pageSize = getAs(parameters, "pageSize", 100);
    int fromLine = getAs(parameters, "fromLine", 1);
    boolean enableTail = getAs(parameters, "enableTail", false);
    if (lastRows > EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue()) {
      throw new ECMErrorException(
          CANNOT_FETCH_MORE_THAN.getErrorCode(),
          MessageFormat.format(
              CANNOT_FETCH_MORE_THAN.getErrorDesc(),
              EngineConnLogOperator.MAX_LOG_FETCH_SIZE.getValue().toString()));
    } else if (lastRows > 0) {
      String logs = Utils.exec(new String[] {"tail", "-n", lastRows + "", logPath.getPath()}, 5000);
      Map<String, Object> stringObjectHashMap = new HashMap<>();
      stringObjectHashMap.put("logs", logs.split("\n"));
      stringObjectHashMap.put("rows", logs.length());
      return stringObjectHashMap;
    }

    String ignoreKeywords = getAs(parameters, "ignoreKeywords", "");
    String[] ignoreKeywordList =
        StringUtils.isNotEmpty(ignoreKeywords) ? ignoreKeywords.split(",") : new String[0];

    String onlyKeywords = getAs(parameters, "onlyKeywords", "");
    String[] onlyKeywordList =
        StringUtils.isNotEmpty(onlyKeywords) ? onlyKeywords.split(",") : new String[0];

    RandomAccessFile randomReader = null;
    ReversedLinesFileReader reversedReader = null;
    try {
      if (enableTail) {
        logger.info("enable log operator from tail to read");
        reversedReader = new ReversedLinesFileReader(logPath, Charset.defaultCharset());
      } else {
        randomReader = new RandomAccessFile(logPath, "r");
      }

      ArrayList<String> logs = new ArrayList<>(pageSize);
      int readLine = 0, skippedLine = 0, lineNum = 0;
      boolean rowIgnore = false;
      int ignoreLine = 0;
      Pattern linePattern = Pattern.compile(EngineConnLogOperator.MULTILINE_PATTERN.getValue());

      int maxMultiline = MULTILINE_MAX.getValue();
      String line = randomAndReversedReadLine(randomReader, reversedReader);

      while (readLine < pageSize && line != null) {
        lineNum += 1;
        if (skippedLine < fromLine - 1) {
          skippedLine += 1;
        } else {
          if (rowIgnore) {
            Matcher matcher = linePattern.matcher(line);
            if (matcher.matches()) {
              ignoreLine = 0;
              rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList);
            } else {
              ignoreLine += 1;
              if (ignoreLine >= maxMultiline) {
                rowIgnore = false;
              }
            }
            if (!matcher.matches()) {
              rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList);
            }
          } else {
            rowIgnore = !includeLine(line, onlyKeywordList, ignoreKeywordList);
          }
          if (!rowIgnore) {
            logs.add(line);
            readLine += 1;
          }
        }
        line = randomAndReversedReadLine(randomReader, reversedReader);
      }

      if (enableTail) {
        Collections.reverse(logs);
      }

      Map<String, Object> resultMap = new HashMap<>();
      resultMap.put("logPath", logPath.getPath());
      resultMap.put("logs", logs);
      resultMap.put("endLine", lineNum);
      resultMap.put("rows", readLine);
      return resultMap;
    } catch (IOException e) {
      logger.info("EngineConnLogOperator apply failed", e);
      throw new ECMErrorException(
          LOG_IS_NOT_EXISTS.getErrorCode(), LOG_IS_NOT_EXISTS.getErrorDesc());
    } finally {
      IOUtils.closeQuietly(randomReader);
      IOUtils.closeQuietly(reversedReader);
    }
  }

  private String randomAndReversedReadLine(
      RandomAccessFile randomReader, ReversedLinesFileReader reversedReader) throws IOException {
    if (randomReader != null) {
      String line = randomReader.readLine();
      if (line != null) {
        return new String(line.getBytes(StandardCharsets.ISO_8859_1), Charset.defaultCharset());
      } else {
        return null;
      }
    } else {
      return reversedReader.readLine();
    }
  }

  protected File getLogPath(Map<String, Object> parameters) {
    String logType = getAs(parameters, "logType", EngineConnLogOperator.LOG_FILE_NAME.getValue());

    Triple<String, String, String> engineConnInfo = getEngineConnInfo(parameters);
    String engineConnLogDir = engineConnInfo.getLeft();
    String engineConnInstance = engineConnInfo.getMiddle();
    String ticketId = engineConnInfo.getRight();

    File logPath = new File(engineConnLogDir, logType);
    if (!logPath.exists() || !logPath.isFile()) {
      throw new ECMErrorException(
          LOGFILE_IS_NOT_EXISTS.getErrorCode(),
          MessageFormat.format(LOGFILE_IS_NOT_EXISTS.getErrorDesc(), logPath.toString()));
    }
    logger.info(
        String.format(
            "Try to fetch EngineConn(id: %s, instance: %s) logs from %s.",
            ticketId, engineConnInstance, logPath.getPath()));
    return logPath;
  }

  protected Triple<String, String, String> getEngineConnInfo(Map<String, Object> parameters) {
    String logDIrSuffix = getAs(parameters, "logDirSuffix", "");
    String engineConnLogDir =
        ECMConfiguration.ENGINECONN_ROOT_DIR() + File.separator + logDIrSuffix;
    String ticketId = getAs(parameters, "ticketId", "");
    String engineConnInstance = "";
    return Triple.of(engineConnLogDir, engineConnInstance, ticketId);
  }

  private boolean includeLine(String line, String[] onlyKeywordList, String[] ignoreKeywordList) {
    boolean accept =
        ignoreKeywordList.length == 0 || !Arrays.stream(ignoreKeywordList).anyMatch(line::contains);
    if (accept) {
      accept =
          onlyKeywordList.length == 0 || Arrays.stream(onlyKeywordList).anyMatch(line::contains);
    }
    return accept;
  }
}
