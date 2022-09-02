import api from '@/common/service/api';

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/udf_tree', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/udf_tree', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/udf_tree', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/udf_tree/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
