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

package org.apache.linkis.engineplugin.elasticsearch.executor.client;

import java.io.IOException;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.sniff.Sniffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EsClient implements EsClientOperate {

  private static final Logger logger = LoggerFactory.getLogger(EsClient.class);

  private String datasourceName;
  public RestClient client;
  private Sniffer sniffer;

  public EsClient(String datasourceName, RestClient client, Sniffer sniffer) {
    this.datasourceName = datasourceName;
    this.client = client;
    this.sniffer = sniffer;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public RestClient getRestClient() {
    return client;
  }

  public Sniffer getSniffer() {
    return sniffer;
  }

  @Override
  public void close() {
    if (sniffer != null) {
      sniffer.close();
    }
    if (client != null) {
      try {
        client.close();
      } catch (IOException e) {
        logger.warn("RestClient close warn");
      }
    }
  }
}
