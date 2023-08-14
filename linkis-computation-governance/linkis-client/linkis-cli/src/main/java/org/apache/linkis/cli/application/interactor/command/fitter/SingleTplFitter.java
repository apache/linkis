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

package org.apache.linkis.cli.application.interactor.command.fitter;

import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.LinkisClientRuntimeException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleTplFitter extends AbstractFitter {
  private static final Logger logger = LoggerFactory.getLogger(SingleTplFitter.class);

  @Override
  public FitterResult fit(String[] input, CmdTemplate templateCopy)
      throws LinkisClientRuntimeException {

    if (input == null || templateCopy == null || input.length == 0) {
      throw new CommandException(
          "CMD0009", ErrorLevel.ERROR, CommonErrMsg.TemplateFitErr, "input or template is null");
    }

    List<String> remains = new ArrayList<>();
    templateCopy = this.doFit(input, templateCopy, remains); // this changes remains List
    return new FitterResult(remains.toArray(new String[remains.size()]), templateCopy);
  }
}
