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
 
package org.apache.linkis.cli.core.presenter.display;

import org.apache.linkis.cli.core.presenter.display.data.FileOutData;
import org.apache.linkis.cli.core.utils.LogUtils;
import org.slf4j.Logger;

/**
 * @description: present output t stdout
 */
public class StdOutDriver implements DisplayDriver {
    @Override
    public void doOutput(Object data) {
        String content = "";
        Logger logger = LogUtils.getPlaintTextLogger();
        if (data instanceof FileOutData) {
            content = ((FileOutData) data).getContent();
        } else if (data instanceof String) {
            content = (String) data;
        }
        logger.info(content);
    }
}