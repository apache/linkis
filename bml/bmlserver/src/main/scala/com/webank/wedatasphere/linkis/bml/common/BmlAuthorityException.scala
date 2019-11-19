package com.webank.wedatasphere.linkis.bml.common

import com.webank.wedatasphere.linkis.common.exception.ErrorException

/**
  * created by cooperyang on 2019/5/21
  * Description:
  */
case class BmlAuthorityException() extends ErrorException(60036, "未登录或登录过期，无法访问物料库")

case class UploadResourceException() extends ErrorException(60050, "首次上传资源失败")

case class UpdateResourceException() extends ErrorException(60051, "更新资源失败")

