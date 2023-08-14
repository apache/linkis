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

package org.apache.linkis.storage.resultset;

import org.apache.linkis.common.io.Fs;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.io.MetaData;
import org.apache.linkis.common.io.Record;
import org.apache.linkis.common.io.resultset.ResultSet;
import org.apache.linkis.storage.FSFactory;
import org.apache.linkis.storage.domain.Dolphin;
import org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary;
import org.apache.linkis.storage.exception.StorageWarnException;
import org.apache.linkis.storage.utils.StorageConfiguration;
import org.apache.linkis.storage.utils.StorageUtils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.storage.errorcode.LinkisStorageErrorCodeSummary.THE_FILE_IS_EMPTY;

public class DefaultResultSetFactory implements ResultSetFactory {

  private static final Logger logger = LoggerFactory.getLogger(DefaultResultSetFactory.class);

  private final Map<String, Class<ResultSet<ResultMetaData, ResultRecord>>> resultClasses;

  private final String[] resultTypes;

  public DefaultResultSetFactory() {
    resultClasses =
        StorageUtils.loadClasses(
            StorageConfiguration.STORAGE_RESULT_SET_CLASSES.getValue(),
            StorageConfiguration.STORAGE_RESULT_SET_PACKAGE.getValue(),
            t -> {
              try {
                return t.newInstance().resultSetType().toLowerCase(Locale.getDefault());
              } catch (InstantiationException e) {
                logger.warn("DefaultResultSetFactory init failed", e);
              } catch (IllegalAccessException e) {
                logger.warn("DefaultResultSetFactory init failed", e);
              }
              return null;
            });
    resultTypes = ResultSetFactory.resultSetType.keySet().toArray(new String[0]);
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSetByType(String resultSetType) {
    if (!resultClasses.containsKey(resultSetType)) {
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorCode(),
          MessageFormat.format(
              LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorDesc(), resultSetType));
    }
    try {
      return resultClasses.get(resultSetType).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new StorageWarnException(
          LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorCode(),
          MessageFormat.format(
              LinkisStorageErrorCodeSummary.UNSUPPORTED_RESULT.getErrorDesc(), resultSetType),
          e);
    }
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(FsPath fsPath)
      throws StorageWarnException {
    return getResultSetByPath(fsPath, StorageUtils.getJvmUser());
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSetByContent(String content) {
    return getResultSetByType(Dolphin.getType(content));
  }

  @Override
  public boolean exists(String resultSetType) {
    return resultClasses.containsKey(resultSetType);
  }

  @Override
  public boolean isResultSetPath(String path) {
    return path.endsWith(Dolphin.DOLPHIN_FILE_SUFFIX);
  }

  @Override
  public boolean isResultSet(String content) {
    try {
      return resultClasses.containsKey(Dolphin.getType(content));
    } catch (Exception e) {
      logger.info("Wrong result Set: " + e.getMessage());
      return false;
    }
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSet(String output)
      throws StorageWarnException {
    return getResultSet(output, StorageUtils.getJvmUser());
  }

  @Override
  public String[] getResultSetType() {
    return Arrays.copyOf(resultTypes, resultTypes.length);
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(FsPath fsPath, Fs fs) {
    try (InputStream inputStream = fs.read(fsPath)) {
      String resultSetType = Dolphin.getType(inputStream);
      if (StringUtils.isEmpty(resultSetType)) {
        throw new StorageWarnException(
            THE_FILE_IS_EMPTY.getErrorCode(),
            MessageFormat.format(THE_FILE_IS_EMPTY.getErrorDesc(), fsPath.getPath()));
      }
      // Utils.tryQuietly(fs::close);
      return getResultSetByType(resultSetType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSetByPath(
      FsPath fsPath, String proxyUser) {
    if (fsPath == null) {
      return null;
    }
    logger.info("Get Result Set By Path:" + fsPath.getPath());
    try (Fs fs = FSFactory.getFsByProxyUser(fsPath, proxyUser)) {
      fs.init(new HashMap<>());
      try (InputStream inputStream = fs.read(fsPath)) {
        String resultSetType = Dolphin.getType(inputStream);
        if (StringUtils.isEmpty(resultSetType)) {
          throw new StorageWarnException(
              THE_FILE_IS_EMPTY.getErrorCode(),
              MessageFormat.format(THE_FILE_IS_EMPTY.getErrorDesc(), fsPath.getPath()));
        }
        IOUtils.closeQuietly(inputStream);
        return getResultSetByType(resultSetType);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResultSet<? extends MetaData, ? extends Record> getResultSet(
      String output, String proxyUser) {
    if (isResultSetPath(output)) {
      return getResultSetByPath(new FsPath(output), proxyUser);
    } else if (isResultSet(output)) {
      return getResultSetByContent(output);
    } else {
      return null;
    }
  }
}
