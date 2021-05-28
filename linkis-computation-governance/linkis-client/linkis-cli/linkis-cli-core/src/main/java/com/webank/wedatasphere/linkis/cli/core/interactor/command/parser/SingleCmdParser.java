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

package com.webank.wedatasphere.linkis.cli.core.interactor.command.parser;


import com.webank.wedatasphere.linkis.cli.common.entity.command.CmdTemplate;
import com.webank.wedatasphere.linkis.cli.common.entity.command.Params;
import com.webank.wedatasphere.linkis.cli.common.exception.error.ErrorLevel;
import com.webank.wedatasphere.linkis.cli.core.exception.CommandException;
import com.webank.wedatasphere.linkis.cli.core.exception.error.CommonErrMsg;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.fitter.FitterResult;
import com.webank.wedatasphere.linkis.cli.core.interactor.command.parser.result.ParseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


/**
 * @program: linkis-cli
 * @description: parse command that contains only one sub-command
 * @author: shangda
 * @create: 2021/02/25 16:40
 */
public class SingleCmdParser extends AbstarctParser {
    private static final Logger logger = LoggerFactory.getLogger(SingleCmdParser.class);

    @Override
    public ParseResult parse(String[] input) {
        checkInit();

        if (input == null || input.length == 0) {
            throw new CommandException("CMD0015", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, template.getCmdType(), "nothing to parse");
        }

        FitterResult result = fitter.fit(input, template);

        String[] remains = result.getRemains();

        if (remains != null && remains.length != 0) {
            throw new CommandException("CMD0022", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, template.getCmdType(), "Cannot parse argument(s): " + Arrays.toString(remains) + ". Please check help message");
        }

        CmdTemplate parsedCopyOfTemplate = result.getParsedTemplateCopy();
        Params param = templateToParams(parsedCopyOfTemplate, mapper);

        return new ParseResult(parsedCopyOfTemplate, param, remains);
    }

//
//    private ParseResult parsePrimary(String[] input) {
//        CommandTemplate templatePri = templateMap.get(primaryTemplateName);
//        if (templatePri == null || !(templatePri instanceof PrimaryTemplate)) {
//            throw new CommandException("CMD0014", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "Cannot find a primary template for: " + primaryTemplateName);
//        }
//        if (input == null || input.length == 0) {
//            throw new CommandException("CMD0013", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, templatePri.getCommandType(), "Parser input is empty");
//        }
//        return standardParse(primaryTemplateIdentifier, input, templatePri);
//    }
//
//    private ParseResult parseSingleSub(String[] remains) {
//        if (remains == null || remains.length == 0) {
//            throw new CommandException("CMD0015", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "nothing to parse");
//        }
//        String subCmdTypeName = remains[0];
//        if (StringUtils.isBlank(subCmdTypeName)) {
//            throw new CommandException("CMD0015", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "nothing to parse");
//        }
//        String[] subArgs;
//        if (FitterUtils.isOption(subCmdTypeName)) {
//            logger.info("User did not input sub-command type. Use Universal template by default");
//            subCmdTypeName = Constants.UNIVERSAL_SUBCMD;
//            subArgs = new String[remains.length];
//            System.arraycopy(remains, 0, subArgs, 0, remains.length);
//        } else {
//            subArgs = new String[remains.length - 1];
//            System.arraycopy(remains, 1, subArgs, 0, remains.length - 1);
//        }
//        CommandTemplate templateSub = templateMap.get(subCmdTypeName);
//        if (templateSub == null || !(templateSub instanceof SubTemplate)) {
//            throw new CommandException("CMD0016", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, "Cannot find a sub template for: " + subCmdTypeName);
//        }
//        String identifier = this.subCmdIdentifierGenerator.generateUniqIdentifier();
//
//        ParseResult resultItem = standardParse(identifier, subArgs, templateSub);
//
//        if (resultItem.getRemains() != null && resultItem.getRemains().length != 0) {
//            throw new CommandException("CMD0022", ErrorLevel.ERROR, CommonErrMsg.ParserParseErr, templateSub.getCommandType(), "Cannot parse argument(s): " + Arrays.toString(resultItem.getRemains()) + ". Please check help message");
//        }
//
//        return resultItem;
//
//    }
//
//    private ParseResult standardParse(String identifier, String[] args, CommandTemplate templateOri) {
//
//    }
}