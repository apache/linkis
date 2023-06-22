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

import org.apache.linkis.common.utils.ZipUtils;
import org.apache.linkis.manager.engineplugin.common.exception.EngineConnPluginErrorException;
import org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.linkis.manager.engineplugin.errorcode.EngineconnCoreErrorCodeSummary.NO_PERMISSION_FILE;

public class DefaultEngineConnBmlResourceGenerator extends AbstractEngineConnBmlResourceGenerator {

  private static final Logger logger =
      LoggerFactory.getLogger(DefaultEngineConnBmlResourceGenerator.class);

  public DefaultEngineConnBmlResourceGenerator() {}

  @Override
  public Map<String, EngineConnLocalizeResource[]> generate(String engineConnType) {
    String[] engineConnDistHomes = getEngineConnDistHomeList(engineConnType);
    Map<String, EngineConnLocalizeResource[]> resultMap = new HashMap<>();
    for (String path : engineConnDistHomes) {

      File versionFile = new File(path);
      logger.info("generate, versionFile:" + path);
      if (!versionFile.isDirectory()) {
        logger.warn("File is not dir {},skip to upload", path);
        continue;
      }
      String key = versionFile.getName();

      try {
        EngineConnLocalizeResource[] engineConnLocalizeResources =
            generateDir(versionFile.getPath());
        resultMap.put(key, engineConnLocalizeResources);
      } catch (Throwable t) {
        logger.error("Generate dir : " + path + " error, msg : " + t.getMessage(), t);
        throw t;
      }
    }

    return resultMap;
  }

  @Override
  public EngineConnLocalizeResource[] generate(String engineConnType, String version) {
    String path = getEngineConnDistHome(engineConnType, version);
    return generateDir(path);
  }

  private EngineConnLocalizeResource[] generateDir(String path) {
    File distFile = new File(path);
    if (!distFile.isDirectory()) {
      logger.warn("File is not dir {},skip to upload", path);
      throw new EngineConnPluginErrorException(
          EngineconnCoreErrorCodeSummary.DIST_IRREGULAR_EXIST.getErrorCode(),
          path + " is not dir, to delete this file then retry");
    }
    logger.info("generateDir, distFile:" + path);
    File[] validFiles =
        distFile.listFiles(
            f ->
                !f.getName().endsWith(".zip")
                    || !new File(path, f.getName().replace(".zip", "")).exists());

    return Arrays.stream(validFiles)
        .map(
            file -> {
              if (file.isFile()) {
                return new EngineConnLocalizeResourceImpl(
                    file.getPath(), file.getName(), file.lastModified(), file.length());
              } else {
                File newFile = new File(path, file.getName() + ".zip");
                if (newFile.exists() && !newFile.delete()) {
                  throw new EngineConnPluginErrorException(
                      NO_PERMISSION_FILE.getErrorCode(),
                      MessageFormat.format(NO_PERMISSION_FILE.getErrorDesc(), newFile));
                }

                ZipUtils.fileToZip(file.getPath(), path, file.getName() + ".zip");
                // If it is a folder, the last update time here is the last update time of the
                // folder, not the last update time of
                // ZIP.(如果是文件夹，这里的最后更新时间，采用文件夹的最后更新时间，而不是ZIP的最后更新时间.)
                return new EngineConnLocalizeResourceImpl(
                    newFile.getPath(), newFile.getName(), file.lastModified(), newFile.length());
              }
            })
        .toArray(EngineConnLocalizeResource[]::new);
  }
}
