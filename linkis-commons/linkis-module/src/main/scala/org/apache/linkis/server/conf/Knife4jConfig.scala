package org.apache.linkis.server.conf

import org.springframework.context.annotation.Bean
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc

@EnableSwagger2WebMvc
@EnableKnife4j
@Configuration
class Knife4jConfig extends WebMvcConfigurer {



  import org.springframework.beans.factory.annotation.Value

  @Value("${spring.application.name}") private val appName = "linkis service"

  @Bean(Array("defaultApi2"))
  def  defaultApi2() : Docket = {
     val docket = new Docket(DocumentationType.SWAGGER_2)
      .apiInfo(apiInfo())
      //分组名称
      .groupName("RESTAPI")
      .select()
      //这里指定Controller扫描包路径
      .apis(RequestHandlerSelectors.basePackage("org.apache.linkis"))
      .paths(PathSelectors.any())
      .build()
    docket
  }

  def  apiInfo() : ApiInfo ={
    val apiInfo = new ApiInfoBuilder()
      .title(appName)
      .description("Linkis micro service RESTful APIs")
      .version("v1")
      .build()
    apiInfo
  }

  override def addResourceHandlers( registry : ResourceHandlerRegistry): Unit  =  {
    registry.addResourceHandler("/api/rest_j/v1/doc.html**").addResourceLocations("classpath:/META-INF/resources/doc.html")
    registry.addResourceHandler("/api/rest_j/v1/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/")
  }

  override def addViewControllers(registry : ViewControllerRegistry ) : Unit = {
    registry.addRedirectViewController("/api/rest_j/v1/v2/api-docs", "/v2/api-docs")
    registry.addRedirectViewController("/api/rest_j/v1/swagger-resources/configuration/ui", "/swagger-resources/configuration/ui")
    registry.addRedirectViewController("/api/rest_j/v1/swagger-resources/configuration/security", "/swagger-resources/configuration/security")
    registry.addRedirectViewController("/api/rest_j/v1/swagger-resources", "/swagger-resources")
  }
}
