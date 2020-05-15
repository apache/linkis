package com.webank.wedatasphere.linkis.entrance.execute

import java.util

import com.webank.wedatasphere.linkis.common.conf.CommonVars
import com.webank.wedatasphere.linkis.entrance.conf.EsEntranceConfiguration
import com.webank.wedatasphere.linkis.scheduler.executer.{ExecuteRequest, JobExecuteRequest, RunTypeExecuteRequest, SuccessExecuteResponse}
import com.webank.wedatasphere.linkis.server.JMap
import org.junit.{Before, Test}

/**
 *
 * @author wang_zh
 * @date 2020/5/14
 */
@Test
class EsEntranceEngineTests {

  val STORE_PATH = "/tmp/test"
  var entranceEngine: EsEntranceEngine = _
  var options: JMap[String, String] = _
  @Before
  def init(): Unit = {
    options = new util.HashMap[String, String]()
    options.put("wds.linkis.es.cluster", "127.0.0.1:9200")
    options.put("wds.linkis.es.datasource", "test")

    options.put("runType", "esjson")

    entranceEngine = new EsEntranceEngine(1, options)
    entranceEngine.init()
  }

  @Test
  def test() = {
//    val query = "{\"query\":{\"match_all\":{}}}"
    val query = "{\"query\":{\"match_all\":{}}}{\"query\":{\"match_all\":{}}}"

    options.put("wds.linkis.es.http.endpoint", "_search?format=text&pretty")
    val executeRequest =  new ExecuteRequest with StorePathExecuteRequest with RunTypeExecuteRequest {
      override val code: String = query
      override val storePath: String = STORE_PATH
      override val runType: String = "esjson"
    }

    val response = entranceEngine.execute(executeRequest)
  }

}
