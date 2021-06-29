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

/**
 * @description: interface for parsing command arguments and fill them into instance of {@link CmdTemplate},
 * note that implementation of this interface should make a deep copy of {@link CmdTemplate} instance
 */
public interface Fitter {
    FitterResult fit(String[] input, CmdTemplate templateCopy) throws LinkisClientRuntimeException;
}
