/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.cli.core.interactor.properties;

import org.apache.linkis.cli.common.constants.CommonConstants;
import org.apache.linkis.cli.common.exception.error.ErrorLevel;
import org.apache.linkis.cli.core.exception.PropsException;
import org.apache.linkis.cli.core.exception.error.CommonErrMsg;
import org.apache.linkis.cli.core.interactor.properties.reader.PropertiesReader;
import org.apache.linkis.cli.core.interactor.properties.reader.PropsFileReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @description: Scanning '.properties' files given root path
 */
public class PropsFilesScanner {
    private static final Logger logger = LoggerFactory.getLogger(PropsFilesScanner.class);

    public List<File> getPropsFiles(String rootPath) {
        logger.info("Start scanning for properties files. Root path = \"{}\"", rootPath);
        if (StringUtils.isBlank(rootPath)) {
            throw new PropsException("PRP0004", ErrorLevel.ERROR, CommonErrMsg.PropsLoaderErr,
                    "Failed to  properties files because rootPath is empty");
        }

        List<File> files;
        try {
            files = (List<File>) FileUtils.listFiles(new File(rootPath), CommonConstants.CONFIG_EXTENSION, false);
        } catch (Exception e) {
            throw new PropsException("PRP0005", ErrorLevel.ERROR, CommonErrMsg.PropsLoaderErr,
                    "Failed to list properties files", e);
        }

        if (files == null || files.size() == 0) {
            throw new PropsException("PRP0006", ErrorLevel.WARN, CommonErrMsg.PropsLoaderErr,
                    "PropsFilesScanner has scanned 0 files given root " + rootPath);
        }
        logger.info("Scanned properties files=\"{}\"", files.toString());
        return files;
    }

    public List<PropertiesReader> getPropsReaders(String rootPath) {
        List<File> files = new PropsFilesScanner().getPropsFiles(rootPath);
        List<PropertiesReader> readersList = new ArrayList<>(); //+1 user config
        for (int i = 0; i < files.size(); i++) {
            // let identifier = fileName
            String name = files.get(i).getName();
            String path = files.get(i).getAbsolutePath();
            PropertiesReader reader = new PropsFileReader();
            reader.setPropsId(name);
            reader.setPropsPath(path);
            readersList.add(reader);
        }
        return readersList;
    }

}