package com.webank.wedatasphere.linkis.engineplugin.elasticsearch.executer.client

import com.webank.wedatasphere.linkis.storage.domain.Column
import com.webank.wedatasphere.linkis.storage.resultset.table.TableRecord

trait ElasticSearchResponse

case class ElasticSearchTableResponse(columns: Array[Column], records: Array[TableRecord]) extends ElasticSearchResponse

case class ElasticSearchJsonResponse(value: String) extends ElasticSearchResponse

case class ElasticSearchErrorResponse(message: String, body: String = null, cause: Throwable = null) extends ElasticSearchResponse
