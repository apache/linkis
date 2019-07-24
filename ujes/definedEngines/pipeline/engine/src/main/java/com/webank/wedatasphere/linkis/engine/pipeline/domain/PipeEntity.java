/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.engine.pipeline.domain;

import com.webank.wedatasphere.linkis.engine.pipeline.exception.PipeLineErrorException;
import com.webank.wedatasphere.linkis.engine.pipeline.parser.IEParser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by johnnwang on 2018/11/14.
 */
public class PipeEntity {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public static final String SET_REGEX = "\\s*set\\s+([a-z]{2,12})\\s*=\\s*(\\S+)\\s?";
    public static final String STORAGE_REGEX = "(?i)\\s*from\\s+(\\S+)\\s+to\\s+(\\S+)\\s?";
    static final Pattern SET_PATTERN = Pattern.compile(SET_REGEX);
    static final Pattern STORAGE_PATTERN = Pattern.compile(STORAGE_REGEX);
    private String source;
    private String dest;
    private String type;
    private Map<String, String> paramMap = new HashMap<String, String>();

    public PipeEntity(String script) throws PipeLineErrorException {


        /**
         * script样式：
         * %pipeLine.toWorkspace
         * set key=value;
         * set key=value;
         * set key=value;
         * set key=value;
         * from source to dest;
         */
        String kind = IEParser.getKind(script);
        if ("pipeLine.toWorkspace".equals(kind)) {
            this.type = "toWorkspace";
        } else if ("pipeLine.toHDFS".equals(kind)) {
            this.type = "toHDFS";
        } else if ("pipeLine.toExcel".equals(kind)) {
            this.type = "toExcel";
        } else if ("pipeLine.toCSV".equals(kind)) {
            this.type = "toCSV";
        } else if ("pipeLine.toTxt".equals(kind)) {
            this.type = "toTxt";
        } else {
            // TODO: 2018/11/16 错误码待定
            LOGGER.error("not support(不支持) " + kind + " Import and export type(的导入导出类型)");
            throw new PipeLineErrorException(70001, "不支持" + kind + "的导入导出类型");
        }
        String code = IEParser.getFormatCode(script);
        if (StringUtils.isEmpty(code)) {
            LOGGER.error("code is Empty!");
            throw new PipeLineErrorException(70002, "The execution code is empty!(执行代码为空!)");
        }
        String splitRegex = ";";
        if (code.indexOf(";") < 0) {
            splitRegex = "\n";
        }
        String[] codes = code.split(splitRegex);
        for (String _code : codes) {
            Matcher matcher = SET_PATTERN.matcher(_code);
            boolean find = matcher.find();
            if (find) {
                String key = matcher.group(1);
                String value = matcher.group(2);
                paramMap.put(key, value);
                //else ignore it.
                continue;
            }
            matcher = STORAGE_PATTERN.matcher(_code);
            find = matcher.find();
            if (find) {
                source = matcher.group(1);
                dest = matcher.group(2);
            } //else ignore it.
        }
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(dest)) {
            LOGGER.error("Import or export address is empty(导入或导出地址为空)");
            throw new PipeLineErrorException(70003, "Import or export address is empty（导入或导出地址为空）");
        }
    }

    @Override
    public String toString() {
        return "PipeEntity{" +
                "source='" + source + '\'' +
                ", dest='" + dest + '\'' +
                ", type='" + type + '\'' +
                ", paramMap=" + paramMap +
                '}';
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }
}
