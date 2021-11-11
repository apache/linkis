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
 
import api from "./api.js"
import API_PATH from '@/common/config/apiPath.js'
import storage from "@/common/helper/storage";
// 获取基本信息接口
const GetBaseInfo = (flag = true) => {
  // 如果缓存里有直接返回
  const baseInfo = storage.get('baseInfo', 'local')
  if (baseInfo && flag) {
    return new Promise((resolve) => {
      resolve(baseInfo)
    })
  } else {
    return api.fetch('dss/getBaseInfo', "get")
  }
}

// 获取编排模式选项卡
const GetDicSecondList = (params) => {
  return api.fetch(`${API_PATH.WORKSPACE_PATH}getDicSecondList`, {
    parentKey: "p_orchestrator_mode",
    workspaceId: params
  })
}
// 获取工作空间用户管理相关数据
const GetWorkspaceUserManagement = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}getWorkspaceUsers`, params, 'get')

// 获取工作空间用户的列表
const GetWorkspaceUserList = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}getAllWorkspaceUsers`, params, 'get')

// 获取工作空间数据
const GetWorkspaceData = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}workspaces/${params}`, 'get')

// 获取工作空间应用商店数据
const GetWorkspaceApplications = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}workspaces/${params}/applications`, {}, {method: 'get', cacheOptions: {time: 3000}})

// 获取工作空间归属部门数据
const GetDepartments = () => api.fetch(`${API_PATH.WORKSPACE_PATH}workspaces/departments`, 'get')

// 判断工作空间是否重复
const CheckWorkspaceNameExist = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}workspaces/exists`, params, 'get')

// 获取流程数据字典数据
const GetDicList = (params) => api.fetch(`${API_PATH.WORKSPACE_PATH}getDicList`, params, 'post')

// 获取工作空间列表或修改
const GetWorkspaceList = (params, method) => api.fetch(`${API_PATH.WORKSPACE_PATH}workspaces`, params, method)

// 获取工作空间基础信息
const GetWorkspaceBaseInfo = (params) =>  api.fetch(`${API_PATH.WORKSPACE_PATH}getWorkspaceBaseInfo`, params, 'get')

// 获取工程的应用领域
const GetAreaMap = () =>  api.fetch(`${API_PATH.PROJECT_PATH}listApplicationAreas`, "get")

export {
  GetDicSecondList,
  GetAreaMap,
  GetBaseInfo,
  GetWorkspaceUserManagement,
  GetWorkspaceUserList,
  GetWorkspaceData,
  GetWorkspaceApplications,
  GetDepartments,
  CheckWorkspaceNameExist,
  GetDicList,
  GetWorkspaceList,
  GetWorkspaceBaseInfo
}