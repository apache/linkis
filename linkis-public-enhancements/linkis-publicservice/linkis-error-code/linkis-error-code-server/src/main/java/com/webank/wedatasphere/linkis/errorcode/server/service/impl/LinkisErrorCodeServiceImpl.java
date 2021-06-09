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
package com.webank.wedatasphere.linkis.errorcode.server.service.impl;

import com.webank.wedatasphere.linkis.errorcode.common.LinkisErrorCode;
import com.webank.wedatasphere.linkis.errorcode.server.dao.ErrorCodeMapper;
import com.webank.wedatasphere.linkis.errorcode.server.service.LinkisErrorCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class LinkisErrorCodeServiceImpl implements LinkisErrorCodeService {


    private static final Logger LOGGER = LoggerFactory.getLogger(LinkisErrorCodeServiceImpl.class);

    @Autowired
    ErrorCodeMapper errorCodeMapper;


    @Override
    public List<LinkisErrorCode> getAllErrorCodes() {
        LOGGER.info("Begin to get all ErrorCodes in server");
        return errorCodeMapper.getAllErrorCodes();
    }
}
