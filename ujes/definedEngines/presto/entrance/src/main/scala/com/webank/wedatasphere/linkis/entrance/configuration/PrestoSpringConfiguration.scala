package com.webank.wedatasphere.linkis.entrance.configuration

import java.util.concurrent.TimeUnit

import com.facebook.presto.client.SocketChannelSocketFactory
import com.webank.wedatasphere.linkis.entrance.EntranceParser
import com.webank.wedatasphere.linkis.entrance.annotation._
import com.webank.wedatasphere.linkis.entrance.configuration.PrestoConfiguration._
import com.webank.wedatasphere.linkis.entrance.execute._
import com.webank.wedatasphere.linkis.entrance.executor.PrestoEntranceEngineExecutorManager
import com.webank.wedatasphere.linkis.scheduler.queue.GroupFactory
import okhttp3.{Credentials, Interceptor, OkHttpClient}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.{Bean, Configuration}


/**
 * Created by yogafire on 2020/5/13
 */
@Configuration
class PrestoSpringConfiguration {

  @EntranceExecutorManagerBeanAnnotation
  def generateEntranceExecutorManager(@GroupFactoryBeanAnnotation.GroupFactoryAutowiredAnnotation groupFactory: GroupFactory,
                                      @EngineBuilderBeanAnnotation.EngineBuilderAutowiredAnnotation engineBuilder: EngineBuilder,
                                      @EngineRequesterBeanAnnotation.EngineRequesterAutowiredAnnotation engineRequester: EngineRequester,
                                      @EngineSelectorBeanAnnotation.EngineSelectorAutowiredAnnotation engineSelector: EngineSelector,
                                      @EngineManagerBeanAnnotation.EngineManagerAutowiredAnnotation engineManager: EngineManager,
                                      @Autowired entranceExecutorRulers: Array[EntranceExecutorRuler]): EntranceExecutorManager =
    new PrestoEntranceEngineExecutorManager(groupFactory, engineBuilder, engineRequester, engineSelector, engineManager, entranceExecutorRulers)

  @EntranceParserBeanAnnotation
  def generateEntranceParser(): EntranceParser = {
    new PrestoEntranceParser()
  }

  @Bean
  def okHttpClient(): OkHttpClient = {
    val builder = new OkHttpClient.Builder
    builder.socketFactory(new SocketChannelSocketFactory)
      .connectTimeout(PRESTO_HTTP_READ_TIME_OUT.getValue, TimeUnit.SECONDS)
      .readTimeout(PRESTO_HTTP_READ_TIME_OUT.getValue, TimeUnit.SECONDS)
    if (PRESTO_URL.getValue.toLowerCase.startsWith("https")) {
      builder.addInterceptor((chain: Interceptor.Chain) => {
        chain.proceed(chain.request().newBuilder()
          .header("Authorization", Credentials.basic(PRESTO_USER_NAME.getValue, PRESTO_PASSWORD.getValue)).build())
      })
      //FIXME build https config
    }
    builder.build
  }

}
