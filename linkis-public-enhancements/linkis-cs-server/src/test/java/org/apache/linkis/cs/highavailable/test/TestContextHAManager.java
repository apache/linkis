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

package org.apache.linkis.cs.highavailable.test;

import org.apache.linkis.DataWorkCloudApplication;
import org.apache.linkis.common.ServiceInstance;
import org.apache.linkis.common.conf.BDPConfiguration;
import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.exception.LinkisException;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.cs.common.entity.source.HAContextID;
import org.apache.linkis.cs.common.exception.CSErrorException;
import org.apache.linkis.cs.highavailable.AbstractContextHAManager;
import org.apache.linkis.cs.highavailable.test.haid.TestHAID;
import org.apache.linkis.cs.highavailable.test.persist.TestPersistence;
import org.apache.linkis.server.BDPJettyServerHelper;
import org.apache.linkis.server.conf.ServerConfiguration;

import org.apache.commons.lang3.StringUtils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.DispatcherType;

import java.util.EnumSet;

import com.google.gson.Gson;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "org.apache.linkis")
public class TestContextHAManager extends SpringBootServletInitializer {

  private static ConfigurableApplicationContext applicationContext;
  private static ServiceInstance serviceInstance;
  private static final Gson gson = new Gson();

  public static void main(String[] args) throws ReflectiveOperationException {

    final SpringApplication application = new SpringApplication(TestContextHAManager.class);
    application.addListeners(
        new ApplicationListener<ApplicationPreparedEvent>() {
          public void onApplicationEvent(ApplicationPreparedEvent applicationPreparedEvent) {
            System.out.println("add config from config server...");
            if (applicationContext == null) {
              applicationContext = applicationPreparedEvent.getApplicationContext();
            }
            System.out.println("initialize DataWorkCloud spring application...");
            initDWCApplication();
          }
        });
    application.addListeners(
        new ApplicationListener<RefreshScopeRefreshedEvent>() {
          public void onApplicationEvent(RefreshScopeRefreshedEvent applicationEvent) {
            System.out.println("refresh config from config server...");
            updateRemoteConfig();
          }
        });
    String listeners = ServerConfiguration.BDP_SERVER_SPRING_APPLICATION_LISTENERS().getValue();
    if (StringUtils.isNotBlank(listeners)) {
      for (String listener : listeners.split(",")) {
        application.addListeners((ApplicationListener<?>) Class.forName(listener).newInstance());
      }
    }
    applicationContext = application.run(args);

    try {
      //            Thread.sleep(3000l);
      AbstractContextHAManager haManager =
          (AbstractContextHAManager) applicationContext.getBean(AbstractContextHAManager.class);
      if (null == haManager) {
        System.err.println("Null haManager!");
        return;
      }
      testHAManager(haManager);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void initDWCApplication() {
    serviceInstance = new ServiceInstance();
    serviceInstance.setApplicationName(
        applicationContext.getEnvironment().getProperty("spring.application.name"));
    serviceInstance.setInstance(
        Utils.getComputerName()
            + ":"
            + applicationContext.getEnvironment().getProperty("server.port"));
    LinkisException.setApplicationName(serviceInstance.getApplicationName());
    LinkisException.setHostname(Utils.getComputerName());
    LinkisException.setHostPort(
        Integer.parseInt(applicationContext.getEnvironment().getProperty("server.port")));
  }

  public static void updateRemoteConfig() {
    addOrUpdateRemoteConfig(applicationContext.getEnvironment(), true);
  }

  public static void addRemoteConfig() {
    addOrUpdateRemoteConfig(applicationContext.getEnvironment(), false);
  }

  private static void addOrUpdateRemoteConfig(Environment env, boolean isUpdateOrNot) {
    StandardEnvironment environment = (StandardEnvironment) env;
    PropertySource propertySource = environment.getPropertySources().get("bootstrapProperties");
    if (propertySource == null) {
      return;
    }
    CompositePropertySource source = (CompositePropertySource) propertySource;
    for (String key : source.getPropertyNames()) {
      Object val = source.getProperty(key);
      if (val == null) {
        continue;
      }
      if (isUpdateOrNot) {
        System.out.println("update remote config => " + key + " = " + source.getProperty(key));
        BDPConfiguration.set(key, val.toString());
      } else {
        System.out.println("add remote config => " + key + " = " + source.getProperty(key));
        BDPConfiguration.setIfNotExists(key, val.toString());
      }
    }
  }

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(DataWorkCloudApplication.class);
  }

  @Bean
  public WebServerFactoryCustomizer<JettyServletWebServerFactory> jettyFactoryCustomizer() {
    return new WebServerFactoryCustomizer<JettyServletWebServerFactory>() {
      public void customize(JettyServletWebServerFactory jettyServletWebServerFactory) {
        jettyServletWebServerFactory.addServerCustomizers(
            new JettyServerCustomizer() {
              public void customize(Server server) {
                Handler[] childHandlersByClass =
                    server.getChildHandlersByClass(WebAppContext.class);
                final WebAppContext webApp = (WebAppContext) childHandlersByClass[0];
                FilterHolder filterHolder = new FilterHolder(CharacterEncodingFilter.class);
                filterHolder.setInitParameter("encoding", Configuration.BDP_ENCODING().getValue());
                filterHolder.setInitParameter("forceEncoding", "true");
                webApp.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));
                // BDPJettyServerHelper.setupRestApiContextHandler(webApp);
                BDPJettyServerHelper.setupSpringRestApiContextHandler(webApp);
                if (ServerConfiguration.BDP_SERVER_SOCKET_MODE().getValue()) {
                  BDPJettyServerHelper.setupControllerServer(webApp);
                }
                if (!ServerConfiguration.BDP_SERVER_DISTINCT_MODE().getValue()) {
                  BDPJettyServerHelper.setupWebAppContext(webApp);
                }
              }
            });
      }
    };
  }

  // test
  private static void testHAManager(AbstractContextHAManager contextHAManager) {
    // 1 test create
    TestHAID haid = new TestHAID();
    try {
      TestPersistence testPersistence = contextHAManager.getContextHAProxy(new TestPersistence());
      HAContextID haContextID = testPersistence.createHAID(haid);
      testPersistence.passHAID(haContextID);
      testPersistence.setContextId(haContextID.getContextId());
    } catch (CSErrorException e) {
      e.printStackTrace();
    }
    System.out.println("Test HaManager End.");
  }

  public static ServiceInstance getServiceInstance() {
    return serviceInstance;
  }
}
