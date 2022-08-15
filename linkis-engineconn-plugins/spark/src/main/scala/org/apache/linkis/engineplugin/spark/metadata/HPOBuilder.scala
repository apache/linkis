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

package org.apache.linkis.engineplugin.spark.metadata

import org.apache.linkis.cs.common.entity.history.metadata.TableOperationType
import org.apache.linkis.cs.common.entity.metadata.CSColumn

import org.apache.hadoop.hive.ql.security.authorization.plugin.HivePrivilegeObject.HivePrivilegeObjectType

import java.util.{List => JList}

/**
 */
object HPOBuilder {

  def apply(
      hivePrivilegeObjectType: HivePrivilegeObjectType,
      dbname: String,
      objectName: String,
      partKeys: JList[String],
      columns: JList[CSColumn],
      commandParams: JList[String]
  ): SparkHiveObject = {
    apply(
      hivePrivilegeObjectType,
      dbname,
      objectName,
      partKeys,
      columns,
      TableOperationType.ACCESS,
      commandParams
    )
  }

  def apply(
      hivePrivilegeObjectType: HivePrivilegeObjectType,
      dbname: String,
      objectName: String,
      partKeys: JList[String],
      columns: JList[CSColumn],
      actionType: TableOperationType,
      commandParams: JList[String]
  ): SparkHiveObject = {
    SparkHiveObject(
      hivePrivilegeObjectType,
      dbname,
      objectName,
      partKeys,
      columns,
      actionType,
      commandParams
    )
  }

  def apply(
      hivePrivilegeObjectType: HivePrivilegeObjectType,
      dbname: String,
      objectName: String,
      partKeys: JList[String],
      columns: JList[CSColumn]
  ): SparkHiveObject = {
    apply(
      hivePrivilegeObjectType,
      dbname,
      objectName,
      partKeys,
      columns,
      TableOperationType.ACCESS,
      null
    )
  }

  def apply(
      hivePrivilegeObjectType: HivePrivilegeObjectType,
      dbname: String,
      objectName: String
  ): SparkHiveObject = {
    apply(hivePrivilegeObjectType, dbname, objectName, TableOperationType.ACCESS)
  }

  def apply(
      hivePrivilegeObjectType: HivePrivilegeObjectType,
      dbname: String,
      objectName: String,
      actionType: TableOperationType
  ): SparkHiveObject = {
    apply(hivePrivilegeObjectType, dbname, objectName, null, null, actionType, null)
  }

}
