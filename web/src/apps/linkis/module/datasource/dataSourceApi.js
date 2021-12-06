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

import api from '@/common/service/api';
import { serialize } from 'object-to-formdata';

/**
 * 获取数据源列表
 */
const getDataSourceList = (params)=>{
  return api.fetch('data_source/info', params, 'get')
}

/**
 * 获取数据源类型列表
 */
const getDataSourceTypeList = ()=>{
  return api.fetch('/data_source/type/all', {}, 'get')
}

/**
 * 获取环境列表
 */
const getEnvList = ()=>{
  return api.fetch('data_source/env', {}, 'get')
}

/**
 * 
 * @returns 获取datasource key定义
 */
const getKeyDefine = (id)=>{
  return api.fetch(`/data_source/key_define/type/${id}`, {}, 'get')
}

/**
 * 创建数据源
 * @param {*} realFormData 
 * @returns 
 */
const createDataSource = (realFormData)=>{
  return api.fetch('data_source/info/json', realFormData)
}

/**
 * 创建数据源 formdata
 * @param {*} realFormData 
 * @returns 
 */
const createDataSourceForm = (realFormData)=>{
  return api.fetch('data_source/info/form', realFormData, {methed: 'post', 'Content-Type': 'text/plain'})
}

/**
 * 更新数据源
 * @param {*} datasourceId 
 * @param {*} data 
 * @returns 
 */
const updateDataSource = (data, datasourceId)=>{
  return api.fetch(`data_source/info/${datasourceId}/json`, data, 'put')
}

/**
 * 
 * @param {数据源id} datasourceId 
 * @param {连接参数} data 
 * @returns 
 */
const saveConnectParams = (datasourceId, data, comment)=>{
  return api.fetch(`/data_source/parameter/${datasourceId}/json`, {connectParams: data, comment})//{connectParams: data, comment}
}

/**
 * 创建数据源 formdata
 * @param {*} realFormData 
 * @returns 
 */
const saveConnectParamsForm = (datasourceId, data, comment)=>{
  const formData = serialize({connectParams: data, comment});
  return api.fetch(`/data_source/parameter/${datasourceId}/form`, formData, {methed: 'post', 'Content-Type': 'text/plain'})
}
  

/**
 * 
 * @param {数据源id} datasourceId 
 * @returns 数据源详情
 */
const getDataSourceByIdAndVersion = (datasourceId, version)=>{
  return api.fetch(`/data_source/info/${datasourceId}/${version}`, {}, 'get')
}
/**
 * 获取版本列表
 * @param {数据源id}} datasourceId 
 * @returns 
 */
const getVersionListByDatasourceId = (datasourceId)=>{
  return api.fetch(`/data_source/${datasourceId}/versions`, {}, 'get')
}
  



/**
 * 设置过期=软删除  
 * @param {数据源id} datasourceId 
 * @returns 
 */
const expire = (datasourceId)=>{
  return api.fetch(`/data_source/info/${datasourceId}/expire`, {}, 'put')
}

/**
 * 发布数据源
 * @param {*} datasourceId 
 * @param {*} versionId 
 * @returns 
 */
const publish = (datasourceId, versionId)=>{
  return api.fetch(`data_source/publish/${datasourceId}/${versionId}`, {}, 'post')
}

/**
 * 连接数据源
 * @param {连接信息} data 
 * @returns 
 */
const connect = (data)=> {
  return api.fetch(`data_source/op/connect/json`, data);
}


//过期done
//创建数据源，创建版本done
//

export  {
  getDataSourceList,
  getDataSourceTypeList,
  getEnvList,
  getKeyDefine,
  createDataSource,
  saveConnectParams,
  getDataSourceByIdAndVersion,
  updateDataSource,
  expire,
  publish,
  connect,
  createDataSourceForm,
  getVersionListByDatasourceId,
  saveConnectParamsForm
}