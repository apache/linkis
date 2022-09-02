import api from '@/common/service/api';

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/rm_external_resource_provier', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/rm_external_resource_provier', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/rm_external_resource_provier', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/rm_external_resource_provier/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
