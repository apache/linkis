package com.webank.wedatasphere.linkis.datasource.client.exception

import com.webank.wedatasphere.linkis.common.exception.ErrorException

class DataSourceClientBuilderException(errorDesc: String) extends ErrorException(31000, errorDesc)
