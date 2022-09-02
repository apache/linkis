import api from '@/common/service/api';
import {serialize} from "object-to-formdata";

const getList = (params)=> {
  console.log(params)
  return api.fetch('/basedata_manager/error_code', params , 'get')
}

const add = (data)=> {
  return api.fetch('/basedata_manager/error_code', data , 'post')
}

const edit = (data)=> {
  return api.fetch('/basedata_manager/error_code', data , 'put')
}

const del = (params)=> {
  return api.fetch(`/basedata_manager/error_code/${params.id}`,'delete')
}

export{
  getList,
  add,
  edit,
  del
}
