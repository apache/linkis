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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter;

import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.exception.LinkisClientRuntimeException;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class SingleTplFitter extends AbstractFitter {
    private static final Logger logger = LoggerFactory.getLogger(SingleTplFitter.class);

    @Override
    public FitterResult fit(String[] input, CmdTemplate templateCopy) throws LinkisClientRuntimeException {

        if (input == null || templateCopy == null || input.length == 0) {
            throw new CommandException("CMD0009", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, "input or template is null");
        }

        List<String> remains = new ArrayList<>();
        templateCopy = this.doFit(input, templateCopy, remains); // this changes remains List
        return new FitterResult(remains.toArray(new String[remains.size()]), templateCopy);
    }
}