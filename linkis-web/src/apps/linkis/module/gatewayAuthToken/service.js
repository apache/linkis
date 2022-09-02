import api from '@/common/service/api';

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/gateway_auth_token', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/gateway_auth_token', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/gateway_auth_token', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/gateway_auth_token/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
