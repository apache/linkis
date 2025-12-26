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

package org.apache.linkis.engineconnplugin.flink.client.result;

import org.apache.linkis.engineconnplugin.flink.client.shims.config.Environment;
import org.apache.linkis.engineconnplugin.flink.client.shims.config.entries.DeploymentEntry;
import org.apache.linkis.engineconnplugin.flink.client.shims.exception.SqlExecutionException;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.runtime.net.ConnectionUtils;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.runtime.types.TypeInfoDataTypeConverter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.stream.Stream;

import static org.apache.linkis.engineconnplugin.flink.client.shims.errorcode.FlinkErrorCodeSummary.*;

public class ResultUtil {

  public static <C> BatchResult<C> createBatchResult(TableSchema schema, ExecutionConfig config) {
    final TypeInformation[] schemaTypeInfos =
        Stream.of(schema.getFieldDataTypes())
            .map(TypeInfoDataTypeConverter::fromDataTypeToTypeInfo)
            .toArray(TypeInformation[]::new);
    final RowTypeInfo outputType = new RowTypeInfo(schemaTypeInfos, schema.getFieldNames());

    return new BatchResult<>(schema, outputType, config);
  }

  public static ChangelogResult createChangelogResult(
      Configuration flinkConfig, Environment env, TableSchema schema, ExecutionConfig config)
      throws SqlExecutionException {
    final TypeInformation[] schemaTypeInfos =
        Stream.of(schema.getFieldDataTypes())
            .map(TypeInfoDataTypeConverter::fromDataTypeToTypeInfo)
            .toArray(TypeInformation[]::new);
    final RowTypeInfo outputType = new RowTypeInfo(schemaTypeInfos, schema.getFieldNames());

    // determine gateway address (and port if possible)
    final InetAddress gatewayAddress = getGatewayAddress(env.getDeployment(), flinkConfig);
    final int gatewayPort = getGatewayPort(env.getDeployment());
    final int maxBufferSize = env.getExecution().getMaxTableResultRows();

    return new ChangelogResult(
        outputType, schema, config, gatewayAddress, gatewayPort, maxBufferSize);
  }

  // --------------------------------------------------------------------------------------------

  private static int getGatewayPort(DeploymentEntry deploy) {
    // try to get address from deployment configuration
    return deploy.getGatewayPort();
  }

  private static InetAddress getGatewayAddress(DeploymentEntry deploy, Configuration flinkConfig)
      throws SqlExecutionException {
    // try to get address from deployment configuration
    final String address = deploy.getGatewayAddress();

    // use manually defined address
    if (!address.isEmpty()) {
      try {
        return InetAddress.getByName(address);
      } catch (UnknownHostException e) {
        throw new SqlExecutionException(NOT_SOCKET_RETRIEVAL.getErrorDesc() + address, e);
      }
    } else {
      // TODO cache this
      // try to get the address by communicating to JobManager
      final String jobManagerAddress = flinkConfig.getString(JobManagerOptions.ADDRESS);
      final int jobManagerPort = flinkConfig.getInteger(JobManagerOptions.PORT);
      if (jobManagerAddress != null && !jobManagerAddress.isEmpty()) {
        try {
          return ConnectionUtils.findConnectingAddress(
              new InetSocketAddress(jobManagerAddress, jobManagerPort),
              deploy.getResponseTimeout(),
              400);
        } catch (Exception e) {
          throw new SqlExecutionException(NOT_DETERMINE_ADDRESS_JOB.getErrorDesc(), e);
        }
      } else {
        try {
          return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
          throw new SqlExecutionException(NOT_DETERMINE_ADDRESS.getErrorDesc(), e);
        }
      }
    }
  }
}
