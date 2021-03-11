/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.wedatasphere.linkis.manager.am;

import com.webank.wedatasphere.linkis.DataWorkCloudApplication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @date 2020/7/7 16:22
 */

public class LinkisManagerApplication {

    private static final Log logger = LogFactory.getLog(LinkisManagerApplication.class);

    public static void main(String[] args) throws ReflectiveOperationException {
        logger.info("Start to running LinkisManagerApplication");
        DataWorkCloudApplication.main(args);
    }
}
