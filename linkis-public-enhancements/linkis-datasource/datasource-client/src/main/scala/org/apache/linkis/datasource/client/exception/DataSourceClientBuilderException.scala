package org.apache.linkis.datasource.client.exception

import org.apache.linkis.common.exception.ErrorException

class DataSourceClientBuilderException(errorDesc: String) extends ErrorException(31000, errorDesc)
