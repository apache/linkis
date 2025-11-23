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

package org.apache.linkis.errorcode.client.handler;

import org.apache.linkis.errorcode.client.ClientConfiguration;
import org.apache.linkis.errorcode.client.manager.LinkisErrorCodeManager;
import org.apache.linkis.errorcode.client.utils.ErrorCodeMatcher;
import org.apache.linkis.errorcode.common.ErrorCode;
import org.apache.linkis.errorcode.common.LinkisErrorCode;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;

import scala.Option;
import scala.Tuple2;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkisErrorCodeHandler
    implements LogErrorCodeHandler, LogFileErrorCodeHandler, ExceptionErrorCodeHandler {

  private static LinkisErrorCodeHandler linkisErrorCodeHandler;

  private final LinkisErrorCodeManager linkisErrorCodeManager =
      LinkisErrorCodeManager.getInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeHandler.class);

  private final long futureTimeOut = ClientConfiguration.FUTURE_TIME_OUT.getValue();

  private final ThreadFactory threadFactory =
      new ThreadFactoryBuilder().setNameFormat("linkis-errorcode-handler-%d").build();
  private final ExecutorService threadPool =
      new ThreadPoolExecutor(
          5,
          200,
          0L,
          TimeUnit.MILLISECONDS,
          new LinkedBlockingQueue<Runnable>(1024),
          threadFactory,
          new ThreadPoolExecutor.AbortPolicy());

  public static LinkisErrorCodeHandler getInstance() {
    if (null == linkisErrorCodeHandler) {
      synchronized (LinkisErrorCodeHandler.class) {
        if (null == linkisErrorCodeHandler) {
          linkisErrorCodeHandler = new LinkisErrorCodeHandler();
        }
      }
    }
    return linkisErrorCodeHandler;
  }

  public LinkisErrorCodeHandler() {}

  static {
    // Initialize our timing thread and other thread pools through the getInstance method.
    getInstance();
  }

  @Override
  public List<ErrorCode> handle(String log) {
    // It also starts a thread, if it exceeds 2 seconds, it will directly report the timeout,
    // and then return an empty List.
    // List<ErrorCode> list = new ArrayList<>();
    Set<ErrorCode> errorCodeSet = new HashSet<>();
    Runnable runnable =
        () -> {
          List<LinkisErrorCode> errorCodes = linkisErrorCodeManager.getLinkisErrorCodes();
          Arrays.stream(log.split("\n"))
              .forEach(
                  singleLog -> {
                    Option<Tuple2<String, String>> match =
                        ErrorCodeMatcher.errorMatch(errorCodes, singleLog);
                    if (match.isDefined()) {
                      errorCodeSet.add(new LinkisErrorCode(match.get()._1, match.get()._2));
                    }
                  });
        };
    Future<?> future = threadPool.submit(runnable);
    try {
      future.get(futureTimeOut, TimeUnit.MILLISECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      LOGGER.error("Failed to parse log in {} ms", futureTimeOut, e);
    }
    return new ArrayList<>(errorCodeSet);
  }

  @Override
  public void handle(List<String> logFilePaths) {
    LOGGER.info("begin to handle logfile list");
    for (String s : logFilePaths) {
      handle(s, 0);
    }
  }

  @Override
  public void handle(String logFilePath, int type) {

    LOGGER.info("begin to handle logFilePath {}", logFilePath);
    // At the end of the file, write "error code information is being generated for you".
    try {
      writeToFile(logFilePath, ERROR_CODE_PRE);
    } catch (Exception e) {
      // If there is a write exception, skip this question directly.
      LOGGER.error("Failed to append error code to log file {}", logFilePath, e);
      return;
    }
    Runnable runnable =
        () -> {
          // Pass in the file address, and then start parsing.
          Set<LinkisErrorCode> errorCodeSet = new HashSet<>();
          LOGGER.info("start to parse error codes for {}", logFilePath);
          try (BufferedReader bufferedReader = new BufferedReader(new FileReader(logFilePath))) {
            List<LinkisErrorCode> errorCodes = linkisErrorCodeManager.getLinkisErrorCodes();
            String log = null;
            while ((log = bufferedReader.readLine()) != null) {
              Option<Tuple2<String, String>> match = ErrorCodeMatcher.errorMatch(errorCodes, log);
              if (match.isDefined()) {
                errorCodeSet.add(new LinkisErrorCode(match.get()._1, match.get()._2));
              }
            }
          } catch (IOException e) {
            LOGGER.error("failed to handle log file {} ", logFilePath, e);
            return;
          }
          try {
            if (errorCodeSet.size() == 0) {
              writeToFile(logFilePath, ERROR_CODE_FAILED);
            } else {
              writeToFile(logFilePath, ERROR_CODE_OK);
              List<LinkisErrorCode> retErrorCodes = new ArrayList<>(errorCodeSet);
              writeToFile(logFilePath, retErrorCodes.toString());
            }
          } catch (Exception e) {
            LOGGER.error("failed to write to errorcodes to {} ", logFilePath, e);
          }
        };
    threadPool.submit(runnable);
    LOGGER.info("put handle into threadPool");
  }

  private void writeToFile(String filePath, String content) throws Exception {
    BufferedWriter bufferedWriter =
        new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true)));
    bufferedWriter.write(content);
    bufferedWriter.write(NEW_LINE);
    bufferedWriter.close();
  }

  /**
   * Read the last few lines of the file <br>
   * Equivalent to the tail command in the Linux system, the read size limit is 2GB.
   *
   * @param filename 文件名
   * @param charset 文件编码格式,传null默认使用defaultCharset
   * @param rows 读取行数
   * @throws IOException
   */
  private String readLastRows(String filename, Charset charset, int rows) throws IOException {
    charset = charset == null ? Charset.defaultCharset() : charset;
    String lineSeparator = NEW_LINE;
    try (RandomAccessFile rf = new RandomAccessFile(filename, "r")) {
      byte[] c = new byte[lineSeparator.getBytes().length];
      for (long pointer = rf.length(), lineSeparatorNum = 0;
          pointer >= 0 && lineSeparatorNum < rows; ) {
        rf.seek(pointer--);
        int readLength = rf.read(c);
        if (readLength != -1 && new String(c, 0, readLength).equals(lineSeparator)) {
          lineSeparatorNum++;
        }
        if (pointer == -1 && lineSeparatorNum < rows) {
          rf.seek(0);
        }
      }
      byte[] tempbytes = new byte[(int) (rf.length() - rf.getFilePointer())];
      rf.readFully(tempbytes);
      return new String(tempbytes, charset);
    }
  }

  @Override
  public List<ErrorCode> handleFileLines(String logFilePath, int line) {
    if (line <= 0) {
      line = 1000;
    }
    String todoLog = null;
    try {
      todoLog = readLastRows(logFilePath, Charsets.UTF_8, line);
    } catch (Exception e) {
      LOGGER.error("failed to read last {} lines in file {}", line, logFilePath, e);
    }
    if (StringUtils.isNotEmpty(todoLog)) {
      return handle(todoLog);
    } else {
      return new ArrayList<>();
    }
  }

  @Override
  public ErrorCode handle(Throwable t) {
    return null;
  }

  /**
   * Because this class is a singleton, it is strictly forbidden to call this method except for
   * local testing.
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    threadPool.shutdown();
  }
}
