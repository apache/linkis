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
 
package org.apache.linkis.instance.label.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.linkis.instance.label.conf.InsLabelConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Use spring cache and caffeine
 */
@ConditionalOnClass(Caffeine.class)
@EnableCaching
@Configuration
public class InsLabelCacheConfiguration extends CachingConfigurerSupport {
    private static final Logger LOG = LoggerFactory.getLogger(InsLabelCacheConfiguration.class);
    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(InsLabelConf.CACHE_EXPIRE_TIME.getValue(), TimeUnit.SECONDS)
                .maximumSize(InsLabelConf.CACHE_MAX_SIZE.getValue()));
        cacheManager.setCacheNames(Arrays.asList(InsLabelConf.CACHE_NAMES.getValue().split(",")));
        return cacheManager;
    }

    @Bean
    @Primary
    @Override
    public KeyGenerator keyGenerator() {
        return new KeyGenerator() {
            static final String SPLIT_SYMBOL = ":";
            final Logger LOG = LoggerFactory.getLogger(KeyGenerator.class);

            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder keyBuilder = new StringBuilder()
                        .append(target.getClass().getSimpleName()).append(SPLIT_SYMBOL)
                        .append(method.getName());
                if(params.length > 0){
                    keyBuilder.append(SPLIT_SYMBOL);
                    for (int i = 0; i < params.length; i++) {
                        //For label toString() is all right
                        keyBuilder.append(params[i]);
                        if (i < params.length - 1) {
                            keyBuilder.append(SPLIT_SYMBOL);
                        }
                    }
                }
                LOG.trace("Generate key: [" + keyBuilder.toString() + "]");
                return keyBuilder.toString();
            }
        };
    }
}
