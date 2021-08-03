import api from '@/common/service/api';
import { serialize } from 'object-to-formdata';

/**
 * 获取数据源列表
 */
const getDataSourceList = (params)=>{
  return api.fetch('datasource/info', params, 'get')
}

/**
 * 获取数据源类型列表
 */
const getDataSourceTypeList = ()=>{
  return api.fetch('/datasource/type/all', {}, 'get')
}

/**
 * 获取环境列表
 */
const getEnvList = ()=>{
  return api.fetch('datasource/env', {}, 'get')
}

/**
 * 
 * @returns 获取datasource key定义
 */
const getKeyDefine = (id)=>{
  return api.fetch(`/datasource/key_define/type/${id}`, {}, 'get')
}

/**
 * 创建数据源
 * @param {*} realFormData 
 * @returns 
 */
const createDataSource = (realFormData)=>{
  return api.fetch('datasource/info/json', realFormData)
}

/**
 * 创建数据源 formdata
 * @param {*} realFormData 
 * @returns 
 */
const createDataSourceForm = (realFormData)=>{
  return api.fetch('datasource/info/form', realFormData, {methed: 'post', 'Content-Type': 'text/plain'})
}

/**
 * 更新数据源
 * @param {*} datasourceId 
 * @param {*} data 
 * @returns 
 */
const updateDataSource = (data, datasourceId)=>{
  return api.fetch(`datasource/info/${datasourceId}/json`, data, 'put')
}

/**
 * 
 * @param {数据源id} datasourceId 
 * @param {连接参数} data 
 * @returns 
 */
const saveConnectParams = (datasourceId, data, comment)=>{
  return api.fetch(`/datasource/parameter/${datasourceId}/json`, {connectParams: data, comment})//{connectParams: data, comment}
}

/**
 * 创建数据源 formdata
 * @param {*} realFormData 
 * @returns 
 */
const saveConnectParamsForm = (datasourceId, data, comment)=>{
  const formData = serialize({connectParams: data, comment});
  return api.fetch(`/datasource/parameter/${datasourceId}/form`, formData, {methed: 'post', 'Content-Type': 'text/plain'})
}
  

/**
 * 
 * @param {数据源id} datasourceId 
 * @returns 数据源详情
 */
const getDataSourceByIdAndVersion = (datasourceId, version)=>{
  return api.fetch(`/datasource/info/${datasourceId}/${version}`, {}, 'get')
}
/**
 * 获取版本列表
 * @param {数据源id}} datasourceId 
 * @returns 
 */
const getVersionListByDatasourceId = (datasourceId)=>{
  return api.fetch(`/datasource/${datasourceId}/versions`, {}, 'get')
}
  



/**
 * 设置过期=软删除  
 * @param {数据源id} datasourceId 
 * @returns 
 */
const expire = (datasourceId)=>{
  return api.fetch(`/datasource/info/${datasourceId}/expire`, {}, 'put')
}

/**
 * 发布数据源
 * @param {*} datasourceId 
 * @param {*} versionId 
 * @returns 
 */
const publish = (datasourceId, versionId)=>{
  return api.fetch(`datasource/publish/${datasourceId}/${versionId}`, {}, 'post')
}

/**
 * 连接数据源
 * @param {连接信息} data 
 * @returns 
 */
const connect = (data)=> {
  return api.fetch(`datasource/op/connect/json`, data);
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