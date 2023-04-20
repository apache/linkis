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

package org.apache.linkis.cli.application.interactor.properties.reader;

import org.apache.linkis.cli.application.exception.PropsException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropsFileReader implements PropertiesReader {
  private static final Logger logger = LoggerFactory.getLogger(PropsFileReader.class);
  private String propsId;
  private String propsPath;

  @Override
  public String getPropsId() {
    return propsId;
  }

  @Override
  public PropertiesReader setPropsId(String identifier) {
    this.propsId = identifier;
    return this;
  }

  @Override
  public String getPropsPath() {
    return propsPath;
  }

  @Override
  public PropsFileReader setPropsPath(String propsPath) {
    File propsFile = new File(propsPath);
    this.propsPath = propsFile.getAbsolutePath();
    return this;
  }

  @Override
  public Properties getProperties() {
    checkInit();
    Properties properties = new Properties();
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(propsPath));
      properties.load(in);
    } catch (Exception e) {
      throw new PropsException(
          "PRP0002", ErrorLevel.ERROR, CommonErrMsg.PropsReaderErr, "Source: " + propsPath, e);
    } finally {
      try {
        in.close();
      } catch (Exception ignore) {
        // ignore
      }
    }

    return properties;
  }

  @Override
  public void checkInit() {
    if (StringUtils.isBlank(propsId) || StringUtils.isBlank(propsPath)) {
      throw new PropsException(
          "PRP0001",
          ErrorLevel.WARN,
          CommonErrMsg.PropsReaderInitErr,
          "properties reader for source: "
              + propsPath
              + " is not inited. because of blank propsId or propsPath");
    }
  }
}
