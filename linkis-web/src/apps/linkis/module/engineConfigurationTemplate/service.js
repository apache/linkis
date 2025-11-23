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

const dataSourceEnvBaseUrl = '/basedata-manager/configuration-template'

const getEngineList = ()=> {
  //window.console.log(params)
  return api.fetch(dataSourceEnvBaseUrl + '/engin-list', 'get')
}

const queryEngineByID = (labelId) => {
  return api.fetch(dataSourceEnvBaseUrl + '/template-list-by-label', {engineLabelId: labelId} , 'get')
}

const deleteTemplateByID = (keyID) => {
  return api.fetch(dataSourceEnvBaseUrl + `/${keyID}`, 'delete')
}

const changeTemplate = (params, editType) => {
  let postData = {...params};
  delete postData['_index'];
  delete postData['_rowKey'];
  delete postData['__ob__'];
  if(editType == 'add') delete postData['id'];
  return api.fetch(dataSourceEnvBaseUrl + '/save', postData, 'post')
}

export{
  getEngineList,
  queryEngineByID,
  deleteTemplateByID,
  changeTemplate
}
