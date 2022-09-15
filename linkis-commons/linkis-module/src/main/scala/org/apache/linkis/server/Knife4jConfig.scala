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

package org.apache.linkis.server

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.web.servlet.config.annotation.{
  ResourceHandlerRegistry,
  ViewControllerRegistry,
  WebMvcConfigurer
}

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j
import springfox.documentation.builders.{ApiInfoBuilder, PathSelectors, RequestHandlerSelectors}
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc

/*
 * it is very easy to enable knife when you want to use apiDoc(based on swagger2)
 * you can follow these steps to enable
 * <pre>
 * 1, open application-linkis.yml and set knife4j.production=false
 * 2, open linkis.properties and set wds.linkis.test.mode=true ## it will be renamed as linkis.test.mode in future release
 * 3, restart the service and you can visit http://ip:port/api/rest_j/v1/doc.html
 *
 * or you can use apidoc by following steps  without enable wds.linkis.test.mode
 * 1, open application-linkis.yml and set knife4j.production=false
 * 2, open linkis.propertes ,and set wds.linkis.server.user.restful.uri.pass.auth=/api/rest_j/v1/doc.html,/api/rest_j/v1/swagger-resources,/api/rest_j/v1/webjars,/api/rest_j/v1/v2/api-docs
 * 3, restart the service and you can visit http://ip:port/api/rest_j/v1/doc.html
 * 4, in your browser,add dataworkcloud_inner_request=true, bdp-user-ticket-id's value and  workspaceId's value into cookie
 * </pre>
 */
@EnableSwagger2WebMvc
@EnableKnife4j
@Configuration
class Knife4jConfig extends WebMvcConfigurer {

  @Value("${spring.application.name}") private var appName = "linkis service"

  @Bean(Array("defaultApi2"))
  def defaultApi2(): Docket = {
    val docket = new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo())
      // set group name
      .groupName("RESTAPI")
      .select()
      // specifies the controller scan package path
      .apis(RequestHandlerSelectors.basePackage("org.apache.linkis"))
      .paths(PathSelectors.any())
      .build()
    docket
  }

  def apiInfo(): ApiInfo = {
    val apiInfo = new ApiInfoBuilder()
      .title(appName)
      .description("Linkis micro service RESTful APIs")
      .version("v1")
      .build()
    apiInfo
  }

  override def addResourceHandlers(registry: ResourceHandlerRegistry): Unit = {
    registry
      .addResourceHandler("/api/rest_j/v1/doc.html**")
      .addResourceLocations("classpath:/META-INF/resources/doc.html")
    registry
      .addResourceHandler("/api/rest_j/v1/webjars/**")
      .addResourceLocations("classpath:/META-INF/resources/webjars/")
  }

  override def addViewControllers(registry: ViewControllerRegistry): Unit = {
    registry.addRedirectViewController("/api/rest_j/v1/v2/api-docs", "/v2/api-docs")
    registry.addRedirectViewController(
      "/api/rest_j/v1/swagger-resources/configuration/ui",
      "/swagger-resources/configuration/ui"
    )
    registry.addRedirectViewController(
      "/api/rest_j/v1/swagger-resources/configuration/security",
      "/swagger-resources/configuration/security"
    )
    registry.addRedirectViewController("/api/rest_j/v1/swagger-resources", "/swagger-resources")
  }

}
