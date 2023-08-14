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

package org.apache.linkis.cli.application.interactor.properties;

import org.apache.linkis.cli.application.exception.PropsException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;
import org.apache.linkis.cli.application.interactor.properties.reader.PropertiesReader;

import java.util.*;

public class PropertiesLoader {
  Map<String, PropertiesReader> readersMap;

  public PropertiesLoader() {
    this.readersMap = new HashMap<>();
  }

  public PropertiesLoader setPropertiesReaders(PropertiesReader[] readers) {
    this.readersMap = new HashMap<>();
    for (PropertiesReader reader : readers) {
      readersMap.put(reader.getPropsId(), reader);
    }
    return this;
  }

  public PropertiesLoader addPropertiesReader(PropertiesReader reader) {
    if (reader != null) {
      readersMap.put(reader.getPropsId(), reader);
    }
    return this;
  }

  public PropertiesLoader addPropertiesReaders(PropertiesReader[] readers) {
    if (readers != null && readers.length > 0) {
      for (PropertiesReader reader : readers) {
        readersMap.put(reader.getPropsId(), reader);
      }
    }
    return this;
  }

  public void removePropertiesReader(String identifier) {
    readersMap.remove(identifier);
  }

  public ClientProperties[] loadProperties() {
    checkInit();
    List<ClientProperties> propsList = new ArrayList<>();
    PropertiesReader readerTmp;
    for (Map.Entry<String, PropertiesReader> entry : readersMap.entrySet()) {
      readerTmp = entry.getValue();
      Properties props = readerTmp.getProperties();
      ClientProperties clientProperties = new ClientProperties();
      clientProperties.putAll(props);
      clientProperties.setPropsId(readerTmp.getPropsId());
      clientProperties.setPropertiesSourcePath(readerTmp.getPropsPath());
      propsList.add(clientProperties);
    }
    return propsList.toArray(new ClientProperties[propsList.size()]);
  }

  public void checkInit() {
    if (readersMap == null || readersMap.size() == 0) {
      throw new PropsException(
          "PRP0003",
          ErrorLevel.ERROR,
          CommonErrMsg.PropsLoaderInitErr,
          "properties loader is not inited because it contains no reader");
    }
  }
}
