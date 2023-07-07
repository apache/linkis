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

package org.apache.linkis.cli.application.interactor.command;

import org.apache.linkis.cli.application.entity.command.CmdTemplate;
import org.apache.linkis.cli.application.entity.command.CmdType;
import org.apache.linkis.cli.application.exception.CommandException;
import org.apache.linkis.cli.application.exception.error.CommonErrMsg;
import org.apache.linkis.cli.application.exception.error.ErrorLevel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CmdTemplateFactory {
  private static Map<String, CmdTemplate> templateMap = new ConcurrentHashMap<>();

  public static void register(CmdTemplate template) {
    if (templateMap.containsKey(template.getCmdType().getName())) {
      throw new CommandException(
          "CMD0022",
          ErrorLevel.ERROR,
          CommonErrMsg.TemplateGenErr,
          "template: \"{0}\" already exists",
          template.getCmdType());
    }
    templateMap.put(template.getCmdType().getName(), template);
  }

  public static boolean isRegistered(CmdType cmdType) {
    return templateMap.containsKey(cmdType.getName());
  }

  public static boolean isRegistered(String cmdType) {
    return templateMap.containsKey(cmdType);
  }

  /*
  should not set value to Original template
  */
  public static CmdTemplate getTemplateOri(CmdType cmdType) {
    return templateMap.get(cmdType.getName());
  }

  public static CmdTemplate getTemplateCopy(CmdType cmdType) {
    return templateMap.get(cmdType.getName()).getCopy();
  }

  public static CmdTemplate getTemplateOri(String cmdType) {
    return templateMap.get(cmdType);
  }

  public static CmdTemplate getTemplateCopy(String cmdType) {
    return templateMap.get(cmdType).getCopy();
  }
}
