import api from '@/common/service/api';

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/datasource_type', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/datasource_type', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/datasource_type', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/datasource_type/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
