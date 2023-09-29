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

package org.apache.linkis.cli.core.interactor.properties.reader;

import org.apache.linkis.cli.core.constants.CommonConstants;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SysPropsReader implements PropertiesReader {
  private static final Logger logger = LoggerFactory.getLogger(SysPropsReader.class);
  private String propsId = CommonConstants.SYSTEM_PROPERTIES_IDENTIFIER;
  private String propsPath = "SYSTEM";

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
  public PropertiesReader setPropsPath(String propertiesPath) {
    this.propsPath = propertiesPath;
    return this;
  }

  @Override
  public Properties getProperties() {
    checkInit();
    return System.getProperties();
  }

  @Override
  public void checkInit() {}
}
