/**
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.wedatasphere.linkis.entrance.executor.esclient

import java.util
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.webank.wedatasphere.linkis.server.JMap
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.{Response, ResponseListener}
import org.junit.Test

/**
 *
 * @author wang_zh
 * @date 2020/5/14
 */
@Test
class EsClientFactoryTests {

  val options: JMap[String, String] = new util.HashMap[String, String]()
  options.put("wds.linkis.es.cluster", "127.0.0.1:9200")
  options.put("wds.linkis.es.datasource", "test")

  @Test
  def test() = {
    options.put("wds.linkis.es.http.endpoint", "_search?format=text&pretty")

    val code = "{\"query\":{\"match_all\":{}}}"

    val client = EsClientFactory.getRestClient(options)
    val countDown = new CountDownLatch(1)

    val cancellable = client.execute(code, options, new ResponseListener {
      override def onSuccess(response: Response): Unit = {
        print(EntityUtils.toString(response.getEntity()))
        countDown.countDown()
      }

      override def onFailure(exception: Exception): Unit = {
        countDown.countDown()
      }
    })

    countDown.await(10, TimeUnit.SECONDS)

    cancellable.cancel()
  }


}
