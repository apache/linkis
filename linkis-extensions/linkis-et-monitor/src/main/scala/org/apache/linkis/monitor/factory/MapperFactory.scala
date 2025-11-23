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

package org.apache.linkis.monitor.factory

import org.apache.linkis.monitor.department.dao.UserDepartmentInfoMapper
import org.apache.linkis.monitor.instance.dao.{
  InsLabelRelationDao,
  InstanceInfoDao,
  InstanceLabelDao
}
import org.apache.linkis.monitor.jobhistory.dao.JobHistoryMapper

object MapperFactory {

  private var jobHistoryMapper: JobHistoryMapper = _

  private var instanceInfoMapper: InstanceInfoDao = _

  private var instanceLabelMapper: InstanceLabelDao = _

  private var instanceLabelRelationMapper: InsLabelRelationDao = _

  private var userDepartmentInfoMapper: UserDepartmentInfoMapper = _

  def getJobHistoryMapper(): JobHistoryMapper = jobHistoryMapper

  def setJobHistoryMapper(jobHistoryMapper: JobHistoryMapper): Unit = {
    MapperFactory.jobHistoryMapper = jobHistoryMapper
  }

  def getInstanceInfoMapper(): InstanceInfoDao = instanceInfoMapper

  def setInstanceInfoMapper(instanceInfoMapper: InstanceInfoDao): Unit = {
    MapperFactory.instanceInfoMapper = instanceInfoMapper
  }

  def getInstanceLabelMapper(): InstanceLabelDao = instanceLabelMapper

  def setInstanceLabelMapper(instanceLabelMapper: InstanceLabelDao): Unit = {
    MapperFactory.instanceLabelMapper = instanceLabelMapper
  }

  def getInsLabelRelationMapper(): InsLabelRelationDao = instanceLabelRelationMapper

  def setInsLabelRelationMapper(instanceLabelRelationMapper: InsLabelRelationDao): Unit = {
    MapperFactory.instanceLabelRelationMapper = instanceLabelRelationMapper
  }

  // 获取userDepartmentInfoMapper的值
  def getUserDepartmentInfoMapper: UserDepartmentInfoMapper = userDepartmentInfoMapper

  // 设置userDepartmentInfoMapper的值
  def setUserDepartmentInfoMapper(mapper: UserDepartmentInfoMapper): Unit = {
    userDepartmentInfoMapper = mapper
  }

}
