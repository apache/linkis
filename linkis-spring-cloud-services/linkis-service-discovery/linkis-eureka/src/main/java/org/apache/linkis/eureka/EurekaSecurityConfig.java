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

package org.apache.linkis.eureka;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EnableWebSecurity
@ConfigurationProperties("auth")
public class EurekaSecurityConfig extends WebSecurityConfigurerAdapter {

  private static final Logger logger = LoggerFactory.getLogger(EurekaSecurityConfig.class);

  private Boolean enableAuth = false;

  public Boolean getEnableAuth() {
    return enableAuth;
  }

  public void setEnableAuth(Boolean enableAuth) {
    this.enableAuth = enableAuth;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
    if (!enableAuth) {
      logger.info("eureka off auth");
      http.authorizeRequests().anyRequest().permitAll().and().logout().permitAll();
    } else {
      logger.info("eureka spring security enable");
      super.configure(http);
    }
  }
}
