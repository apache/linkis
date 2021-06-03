/*
 * Copyright 2019 WeBank
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.cli.core.interactor.properties;

import com.webank.wedatasphere.linkis.cli.common.entity.properties.ClientProperties;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.PropsException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.exception.handler.DefaultExceptionHandler;
import com.webank.wedatasphere.linkis.cli.core.interactor.properties.reader.PropertiesReader;

import java.util.*;


public class StdPropsLoader implements PropertiesLoader {
    Map<String, PropertiesReader> readersMap;

    public StdPropsLoader() {
        this.readersMap = new HashMap<>();
    }

    @Override
    public PropertiesLoader setPropertiesReaders(PropertiesReader[] readers) {
        this.readersMap = new HashMap<>();
        for (PropertiesReader reader : readers) {
            readersMap.put(reader.getPropsId(), reader);
        }
        return this;
    }

    @Override
    public PropertiesLoader addPropertiesReader(PropertiesReader reader) {
        if (reader != null) {
            readersMap.put(reader.getPropsId(), reader);
        }
        return this;
    }

    @Override
    public PropertiesLoader addPropertiesReaders(PropertiesReader[] readers) {
        if (readers != null && readers.length > 0) {
            for (PropertiesReader reader : readers) {
                readersMap.put(reader.getPropsId(), reader);
            }
        }
        return this;
    }

    @Override
    public void removePropertiesReader(String identifier) {
        readersMap.remove(identifier);
    }


    @Override
    public ClientProperties[] loadProperties() {
        checkInit();
        List<ClientProperties> propsList = new ArrayList<>();
        PropertiesReader readerTmp;
        for (Map.Entry<String, PropertiesReader> entry : readersMap.entrySet()) {
            readerTmp = entry.getValue();
            try {
                Properties props = readerTmp.getProperties();
                ClientProperties clientProperties = new ClientProperties();
                clientProperties.putAll(props);
                clientProperties.setPropsId(readerTmp.getPropsId());
                clientProperties.setPropertiesSourcePath(readerTmp.getPropsPath());
                propsList.add(clientProperties);
            } catch (Exception e) {
                new DefaultExceptionHandler().handle(e);
            }
        }
        return propsList.toArray(new ClientProperties[propsList.size()]);
    }

    @Override
    public void checkInit() {
        if (readersMap == null || readersMap.size() == 0) {
            throw new PropsException("PRP0003", ErrorLevel.ERROR, CommonErrMsg.PropsLoaderInitErr,
                    "properties loader is not inited because it contains no reader");
        }
    }
}