import api from '@/common/service/api';

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/udf_manager', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/udf_manager', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/udf_manager', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/udf_manager/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
