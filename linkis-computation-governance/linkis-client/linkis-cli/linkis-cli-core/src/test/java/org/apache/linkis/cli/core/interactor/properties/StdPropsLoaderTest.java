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

package org.apache.linkis.cli.core.interactor.properties;

import org.apache.linkis.cli.common.entity.properties.ClientProperties;
import org.apache.linkis.cli.core.interactor.properties.reader.PropertiesReader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StdPropsLoaderTest {
  PropertiesLoader loader;

  @BeforeEach
  public void before() throws Exception {
    System.setProperty("conf.root", "src/test/resources/conf/");
    System.setProperty("conf.file", "linkis-cli.properties");
    String configPath = System.getProperty("conf.root");
    String defaultConfFileName = System.getProperty("conf.file");
    /*
     default config, -Dconf.root & -Dconf.file specifies config path
    */
    List<PropertiesReader> readersList =
        new PropsFilesScanner().getPropsReaders(configPath); // +1 user config
    /*
     user defined config
    */
    // load all config files
    loader =
        new StdPropsLoader()
            .addPropertiesReaders(readersList.toArray(new PropertiesReader[readersList.size()]));
  }

  @AfterEach
  public void after() throws Exception {}

  /** Method: setPropertiesReaders(PropertiesReader[] readers) */
  @Test
  public void testSetPropertiesReaders() throws Exception {
    // TODO: Test goes here...
    loader.setPropertiesReaders(new PropertiesReader[0]);
  }

  /** Method: getAllReadersAsMap() */
  @Test
  public void testGetAllReadersAsMap() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: addPropertiesReader(PropertiesReader reader) */
  @Test
  public void testAddPropertiesReader() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: addPropertiesReaders(PropertiesReader[] readers) */
  @Test
  public void testAddPropertiesReaders() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: getPropertiesReader(String identifier) */
  @Test
  public void testGetPropertiesReader() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: removePropertiesReader(String identifier) */
  @Test
  public void testRemovePropertiesReader() throws Exception {
    // TODO: Test goes here...
  }

  /** Method: loadProperties() */
  @Test
  public void testLoadProperties() throws Exception {
    ClientProperties[] loaderResult = loader.loadProperties();

    List<ClientProperties> properties =
        Arrays.stream(loaderResult)
            .sorted((p1, p2) -> p1.size() - p2.size())
            .collect(Collectors.toList());

    assertEquals(2, properties.size());
    assertEquals(properties.get(0).getPropsId(), "user.properties");
    assertEquals(properties.get(0).size(), 4);

    assertEquals(properties.get(1).getPropsId(), "linkis-cli.properties");
    assertEquals(properties.get(1).size(), 8);
  }

  /** Method: checkInit() */
  @Test
  public void testCheckInit() throws Exception {
    // TODO: Test goes here...
  }
}
