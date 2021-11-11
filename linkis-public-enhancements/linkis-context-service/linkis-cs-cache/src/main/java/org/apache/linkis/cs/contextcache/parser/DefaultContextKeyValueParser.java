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
 
package org.apache.linkis.cs.contextcache.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.linkis.cs.common.entity.source.ContextKeyValue;
import org.apache.linkis.cs.contextcache.conf.ContextCacheConf;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Component
public class DefaultContextKeyValueParser implements ContextKeyValueParser {

    private static final Logger logger = LoggerFactory.getLogger(DefaultContextKeyValueParser.class);


    private ObjectMapper jackson = BDPJettyServerHelper.jacksonJson();

    @PostConstruct
    private void init() {
        logger.info("init keyValueParser");
    }


    @Override
    public Set<String> parse(ContextKeyValue contextKeyValue) {
        //先解析key
        Set<String> keywordSet = new HashSet<>();
        try {
            if (contextKeyValue != null && contextKeyValue.getContextValue() != null && StringUtils.isNotBlank(contextKeyValue.getContextValue().getKeywords())){
                String keywordObj = contextKeyValue.getContextValue().getKeywords();

                try {
                    Set<String> keySet = jackson.readValue(keywordObj, new TypeReference<Set<String>>() {});
                    keywordSet.addAll(keySet);
                 } catch (Exception e) {
                    //TODO Delete later
                    logger.info("deal Exception", e);
                    String[] keywords = keywordObj.split(ContextCacheConf.KEYWORD_SPLIT);
                    for (String keyword: keywords){
                        keywordSet.add(keyword);
                    }
                }
                //TODO The contextKey default are keyword
                keywordSet.add(contextKeyValue.getContextKey().getKey());
            }
        } catch (Exception e){
            if (null != contextKeyValue && null != contextKeyValue.getContextKey() && StringUtils.isNotBlank(contextKeyValue.getContextKey().getKey())){
                logger.error("Failed to parse keywords of " + contextKeyValue.getContextKey().getKey(), e);
            } else {
                logger.error("Failed to parse keywords of contextKey", e);
            }

        }
        return keywordSet;
    }

}
