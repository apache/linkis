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


const getDataSourceList = (params)=>{
  return api.fetch('data-source-manager/info', params, 'get')
}

const getDataSourceTypeList = ()=>{
  return api.fetch('/data-source-manager/type/all', {}, 'get')
}

const getEnvList = ()=>{
  return api.fetch('data-source-manager/env', {}, 'get')
}

const getKeyDefine = (id)=>{
  return api.fetch(`/data-source-manager/key-define/type/${id}`, {}, 'get')
}

/**
 * createDataSource
 * @param {*} realFormData
 * @returns
 */
const createDataSource = (realFormData)=>{
  return api.fetch('data-source-manager/info/json', realFormData)
}

/**
 * createDataSource formdata
 * @param {*} realFormData
 * @returns
 */
const createDataSourceForm = (realFormData)=>{
  return api.fetch('data-source-manager/info/form', realFormData, {method: 'post', 'Content-Type': 'text/plain'})
}

/**
 * @param {*} datasourceId
 * @param {*} data
 * @returns
 */
const updateDataSource = (data, datasourceId)=>{
  return api.fetch(`data-source-manager/info/${datasourceId}/json`, data, 'put')
}

/**
 *
 * @param  datasourceId
 * @param  data
 * @returns
 */
const saveConnectParams = (datasourceId, data, comment)=>{
  return api.fetch(`/data-source-manager/parameter/${datasourceId}/json`, {connectParams: data, comment})//{connectParams: data, comment}
}

/**
 * @param {*} realFormData
 * @returns
 */
const saveConnectParamsForm = (datasourceId, data, comment)=>{
  const formData = serialize({connectParams: data, comment});
  return api.fetch(`/data-source-manager/parameter/${datasourceId}/form`, formData, {method: 'post', 'Content-Type': 'text/plain'})
}


/**
 * @param  datasourceId
 * @returns
 */
const getDataSourceByIdAndVersion = (datasourceId, version)=>{
  return api.fetch(`/data-source-manager/info/${datasourceId}/${version}`, {}, 'get')
}
/**
 * @param datasourceId
 * @returns
 */
const getVersionListByDatasourceId = (datasourceId)=>{
  return api.fetch(`/data-source-manager/${datasourceId}/versions`, {}, 'get')
}




/**
 * @param datasourceId
 * @returns
 */
const expire = (datasourceId)=>{
  return api.fetch(`/data-source-manager/info/${datasourceId}/expire`, {}, 'put')
}

/**
 * publish datasource
 * @param {*} datasourceId
 * @param {*} versionId
 * @returns
 */
const publish = (datasourceId, versionId)=>{
  return api.fetch(`data-source-manager/publish/${datasourceId}/${versionId}`, {}, 'post')
}

/**
 * @param data
 * @returns
 */
const connect = (data)=> {
  return api.fetch(`data-source-manager/op/connect/json`, data);
}

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
