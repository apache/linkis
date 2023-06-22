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

package org.apache.linkis.engineplugin.server.localize;

import org.apache.linkis.engineplugin.server.conf.EngineConnPluginConfiguration;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary.*;

public abstract class AbstractEngineConnBmlResourceGenerator
    implements EngineConnBmlResourceGenerator {

  private static final Logger logger =
      LoggerFactory.getLogger(AbstractEngineConnBmlResourceGenerator.class);

  public AbstractEngineConnBmlResourceGenerator() {
    if (!new File(getEngineConnsHome()).exists()) {
      throw new EngineConnPluginErrorException(
          CANNOT_HOME_PATH_EC.getErrorCode(),
          MessageFormat.format(CANNOT_HOME_PATH_EC.getErrorDesc(), getEngineConnsHome()));
    }
  }

  public String getEngineConnsHome() {
    return EngineConnPluginConfiguration.ENGINE_CONN_HOME.getValue();
  }

  protected String getEngineConnDistHome(EngineTypeLabel engineConnTypeLabel) {
    return getEngineConnDistHome(
        engineConnTypeLabel.getEngineType(), engineConnTypeLabel.getVersion());
  }

  protected String getEngineConnDistHome(String engineConnType, String version) {
    String engineConnDistHome =
        Paths.get(getEngineConnsHome(), engineConnType, "dist").toFile().getPath();
    checkEngineConnDistHome(engineConnDistHome);
    if (StringUtils.isBlank(version)
        || EngineConnBmlResourceGenerator.NO_VERSION_MARK.equals(version)) {
      return engineConnDistHome;
    }
    String engineConnPackageHome = Paths.get(engineConnDistHome, version).toFile().getPath();
    logger.info("getEngineConnDistHome, engineConnPackageHome path:" + engineConnPackageHome);
    File engineConnPackageHomeFile = new File(engineConnPackageHome);
    if (!engineConnPackageHomeFile.exists()) {
      if (!version.startsWith("v")
          && (boolean) EngineConnPluginConfiguration.EC_BML_VERSION_MAY_WITH_PREFIX_V.getValue()) {
        String versionOld = "v" + version;
        String engineConnPackageHomeOld =
            Paths.get(engineConnDistHome, versionOld).toFile().getPath();
        logger.info(
            "try to getEngineConnDistHome with prefix v, engineConnPackageHome path:"
                + engineConnPackageHomeOld);
        File engineConnPackageHomeFileOld = new File(engineConnPackageHomeOld);
        if (!engineConnPackageHomeFileOld.exists()) {
          throw new EngineConnPluginErrorException(
              ENGINE_VERSION_NOT_FOUND.getErrorCode(),
              MessageFormat.format(
                  ENGINE_VERSION_NOT_FOUND.getErrorDesc(), version, engineConnType));
        } else {
          return engineConnPackageHomeOld;
        }
      } else {
        throw new EngineConnPluginErrorException(
            ENGINE_VERSION_NOT_FOUND.getErrorCode(),
            MessageFormat.format(ENGINE_VERSION_NOT_FOUND.getErrorDesc(), version, engineConnType));
      }
    } else {
      return engineConnPackageHome;
    }
  }

  private void checkEngineConnDistHome(String engineConnPackageHomePath) {
    File engineConnPackageHomeFile = new File(engineConnPackageHomePath);
    checkEngineConnDistHome(engineConnPackageHomeFile);
  }

  private void checkEngineConnDistHome(File engineConnPackageHome) {
    if (!engineConnPackageHome.exists()) {
      throw new EngineConnPluginErrorException(
          CANNOT_HOME_PATH_DIST.getErrorCode(),
          MessageFormat.format(
              CANNOT_HOME_PATH_DIST.getErrorDesc(), engineConnPackageHome.getPath()));
    }
  }

  protected String[] getEngineConnDistHomeList(String engineConnType) {
    String engineConnDistHome =
        Paths.get(getEngineConnsHome(), engineConnType, "dist").toFile().getPath();
    File engineConnDistHomeFile = new File(engineConnDistHome);
    checkEngineConnDistHome(engineConnDistHomeFile);
    File[] children = engineConnDistHomeFile.listFiles();
    if (children.length == 0) {
      throw new EngineConnPluginErrorException(
          DIST_IS_EMPTY.getErrorCode(),
          MessageFormat.format(DIST_IS_EMPTY.getErrorDesc(), engineConnType));
    } else {
      return Arrays.stream(children).map(File::getPath).toArray(String[]::new);
    }
  }

  @Override
  public String[] getEngineConnTypeListFromDisk() {
    return Arrays.stream(new File(getEngineConnsHome()).listFiles())
        .filter(file -> !file.isHidden() && file.isDirectory())
        .map(file -> file.getName())
        .toArray(String[]::new);
  }
}
