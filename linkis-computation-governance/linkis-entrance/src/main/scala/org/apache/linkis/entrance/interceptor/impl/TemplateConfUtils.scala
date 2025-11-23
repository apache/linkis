/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.linkis.entrance.interceptor.impl

import org.apache.linkis.common.conf.Configuration
import org.apache.linkis.common.exception.LinkisCommonErrorException
import org.apache.linkis.common.log.LogUtils
import org.apache.linkis.common.utils.{CodeAndRunTypeUtils, Logging, Utils}
import org.apache.linkis.common.utils.CodeAndRunTypeUtils.LANGUAGE_TYPE_AI_SQL
import org.apache.linkis.entrance.conf.EntranceConfiguration
import org.apache.linkis.entrance.utils.EntranceUtils
import org.apache.linkis.governance.common.entity.TemplateConfKey
import org.apache.linkis.governance.common.entity.job.JobRequest
import org.apache.linkis.governance.common.protocol.conf.{TemplateConfRequest, TemplateConfResponse}
import org.apache.linkis.manager.label.builder.factory.LabelBuilderFactoryContext
import org.apache.linkis.manager.label.conf.LabelCommonConfig
import org.apache.linkis.manager.label.constant.LabelKeyConstant
import org.apache.linkis.manager.label.entity.engine.{
  EngineType,
  EngineTypeLabel,
  FixedEngineConnLabel
}
import org.apache.linkis.manager.label.entity.entrance.ExecuteOnceLabel
import org.apache.linkis.manager.label.utils.LabelUtil
import org.apache.linkis.protocol.utils.TaskUtils
import org.apache.linkis.rpc.Sender
import org.apache.linkis.server.BDPJettyServerHelper

import org.apache.commons.lang3.StringUtils

import java.{lang, util}
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._
import scala.util.matching.{Regex, UnanchoredRegex}

import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}

object TemplateConfUtils extends Logging {

  val confTemplateNameKey = "ec.resource.name"
  val confFixedEngineConnLabelKey = "ec.fixed.sessionId"

  /**
   * 按模板uuid缓存模板配置
   */
  private val templateCache: LoadingCache[String, util.List[TemplateConfKey]] = CacheBuilder
    .newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build(new CacheLoader[String, util.List[TemplateConfKey]]() {

      override def load(templateUuid: String): util.List[TemplateConfKey] = {
        var templateList = Utils.tryAndWarn {
          val sender: Sender = Sender
            .getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)

          logger.info(s"load template configuration data templateUuid:$templateUuid")
          val res = sender.ask(new TemplateConfRequest(templateUuid)) match {
            case response: TemplateConfResponse =>
              logger
                .debug(s"${response.getList()}")
              response.getList
            case _ =>
              logger
                .warn(s"load template configuration data templateUuid:$templateUuid loading failed")
              new util.ArrayList[TemplateConfKey](0)
          }
          res
        }
        if (templateList.size() == 0) {
          logger.warn(s"template configuration data loading failed, plaese check warn log")
        }
        templateList
      }

    })

  /**
   * 按模板名称缓存模板配置
   */
  private val templateCacheName: LoadingCache[String, util.List[TemplateConfKey]] = CacheBuilder
    .newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(5, TimeUnit.MINUTES)
    .build(new CacheLoader[String, util.List[TemplateConfKey]]() {

      override def load(templateName: String): util.List[TemplateConfKey] = {
        var templateList = Utils.tryAndWarn {
          val sender: Sender = Sender
            .getSender(Configuration.CLOUD_CONSOLE_CONFIGURATION_SPRING_APPLICATION_NAME.getValue)

          logger.info(s"load template configuration data templateName:$templateName")
          val res = sender.ask(new TemplateConfRequest(null, templateName)) match {
            case response: TemplateConfResponse =>
              logger
                .debug(s"${response.getList()}")
              response.getList
            case _ =>
              logger
                .warn(s"load template configuration data templateName:$templateName loading failed")
              new util.ArrayList[TemplateConfKey](0)
          }
          res
        }

        if (templateList.size() == 0) {
          logger.warn(s"template configuration data loading failed, plaese check warn log")
        }
        templateList
      }

    })

  /**
   * Get user-defined template conf name value
   *
   * @param code
   *   :code
   * @param codeType
   *   :sql,hql,scala
   * @return
   *   String the last one of template conf name
   */
  def getCustomTemplateConfName(
      jobRequest: JobRequest,
      codeType: String,
      logAppender: lang.StringBuilder
  ): String = {
    var code = jobRequest.getExecutionCode
    var templateConfName = "";

    var varString: String = null
    var errString: String = null
    var fixECString: String = null

    val languageType = CodeAndRunTypeUtils.getLanguageTypeByCodeType(codeType)

    languageType match {
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SQL =>
        varString = s"""\\s*---@set ${confTemplateNameKey}=\\s*.+\\s*"""
        fixECString = s"""\\s*---@set\\s+${confFixedEngineConnLabelKey}\\s*=\\s*([^;]+)(?:\\s*;)?"""
        errString = """\s*---@.*"""
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_PYTHON | CodeAndRunTypeUtils.LANGUAGE_TYPE_SHELL =>
        varString = s"""\\s*##@set ${confTemplateNameKey}=\\s*.+\\s*"""
        fixECString = s"""\\s*##@set\\s+${confFixedEngineConnLabelKey}\\s*=\\s*([^;]+)(?:\\s*;)?"""
        errString = """\s*##@"""
      case CodeAndRunTypeUtils.LANGUAGE_TYPE_SCALA =>
        varString = s"""\\s*///@set ${confTemplateNameKey}=\\s*.+\\s*"""
        fixECString = s"""\\s*///@set\\s+${confFixedEngineConnLabelKey}\\s*=\\s*([^;]+)(?:\\s*;)?"""
        errString = """\s*///@.+"""
      case _ =>
        return templateConfName
    }

    val customRegex = varString.r.unanchored
    val fixECRegex: UnanchoredRegex = fixECString.r.unanchored
    val errRegex = errString.r.unanchored
    var codeRes = code.replaceAll("\r\n", "\n")

    // 匹配任意行，只能是单独的行
    if (codeRes.contains(confTemplateNameKey) || codeRes.contains(confFixedEngineConnLabelKey)) {
      val res = codeRes.split("\n")
      // 用于标识，匹配到就退出
      var matchFlag = false
      res.foreach(str => {
        if (matchFlag) {
          return templateConfName
        }
        str match {
          case customRegex() =>
            val clearStr = if (str.endsWith(";")) str.substring(0, str.length - 1) else str
            val res: Array[String] = clearStr.split("=")
            if (res != null && res.length == 2) {
              templateConfName = res(1).trim
              logger.info(s"get template conf name $templateConfName")
            } else {
              if (res.length > 2) {
                throw new LinkisCommonErrorException(
                  20044,
                  s"$str template conf name var defined uncorrectly"
                )
              } else {
                throw new LinkisCommonErrorException(
                  20045,
                  s"template conf name var  was defined uncorrectly:$str"
                )
              }
            }
            matchFlag = true
          case fixECRegex(sessionId) =>
            // deal with fixedEngineConn configuration, add fixedEngineConn label if setting @set ec.fixed.sessionId=xxx
            if (StringUtils.isNotBlank(sessionId)) {
              val fixedEngineConnLabel =
                LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(
                  classOf[FixedEngineConnLabel]
                )
              fixedEngineConnLabel.setSessionId(sessionId)
              jobRequest.getLabels.add(fixedEngineConnLabel)
              logger.info(
                s"The task ${jobRequest.getId} is set to fixed engine conn, labelValue: ${sessionId}"
              )
              logAppender.append(
                s"The task ${jobRequest.getId} is set to fixed engine conn, labelValue: ${sessionId}"
              )
            } else {
              logger.info(s"The task ${jobRequest.getId} not set fixed engine conn")
            }
            matchFlag = true
          case errRegex() =>
            logger.warn(
              s"The template conf name var definition is incorrect:$str,if it is not used, it will not run the error, but it is recommended to use the correct specification to define"
            )
          case _ =>
        }
      })
    }
    templateConfName
  }

  def dealWithTemplateConf(jobRequest: JobRequest, logAppender: lang.StringBuilder): JobRequest = {
    jobRequest match {
      case requestPersistTask: JobRequest =>
        val params = requestPersistTask.getParams
        val startMap = TaskUtils.getStartupMap(params)
        val runtimeMap: util.Map[String, AnyRef] = TaskUtils.getRuntimeMap(params)

        var templateConflist: util.List[TemplateConfKey] = new util.ArrayList[TemplateConfKey]()
        var templateName: String = ""
        // only for Creator:IDE, try to get template conf name from code string. eg:---@set ec.resource.name=xxxx
        val codeType = LabelUtil.getCodeType(jobRequest.getLabels)
        val (user, creator) = LabelUtil.getUserCreator(jobRequest.getLabels)
        if (EntranceConfiguration.DEFAULT_REQUEST_APPLICATION_NAME.getValue.equals(creator)) {
          templateName = getCustomTemplateConfName(jobRequest, codeType, logAppender)
          if (StringUtils.isNotBlank(templateName)) {
            logAppender.append(
              LogUtils
                .generateInfo(s"Try to execute task with template: $templateName in script.\n")
            )
          }
        }

        // 处理runtime参数中的模板名称，用于失败任务重试的时候使用模板参数重试
        var runtimeTemplateFlag = false
        if (
            EntranceConfiguration.SUPPORT_TEMPLATE_CONF_RETRY_ENABLE.getValue && StringUtils
              .isBlank(templateName)
        ) {
          templateName =
            runtimeMap.getOrDefault(LabelKeyConstant.TEMPLATE_CONF_NAME_KEY, "").toString
          if (StringUtils.isNotBlank(templateName)) {
            runtimeTemplateFlag = true
            logAppender.append(
              LogUtils.generateInfo(
                s"Try to execute task with template: $templateName in runtime params.\n"
              )
            )
          }
        }

        // code template name > start params template uuid
        if (StringUtils.isBlank(templateName)) {
          logger.debug("jobRequest startMap param template name is empty")

          logger.info("jobRequest startMap params :{} ", startMap)
          val templateUuid = startMap.getOrDefault(LabelKeyConstant.TEMPLATE_CONF_KEY, "").toString

          if (StringUtils.isBlank(templateUuid)) {
            logger.debug("jobRequest startMap param template id is empty")
          } else {
            logger.info("try to get template conf list with template uid:{} ", templateUuid)
            logAppender.append(
              LogUtils
                .generateInfo(s"Try to get template conf data with template uid:$templateUuid\n")
            )
            templateConflist = templateCache.get(templateUuid)
            if (templateConflist == null || templateConflist.size() == 0) {
              logAppender.append(
                LogUtils.generateInfo(
                  s"Can not get any template conf data with template uid:$templateUuid\n"
                )
              )
            } else {
              templateName = templateConflist.get(0).getTemplateName
            }
          }
        } else {
          logger.info("Try to get template conf list with template name:[{}]", templateName)
          logAppender.append(
            LogUtils
              .generateInfo(s"Try to get template conf data with template name:[$templateName]\n")
          )
          val cacheList: util.List[TemplateConfKey] = templateCacheName.get(templateName)
          templateConflist.addAll(cacheList)
          if (templateConflist == null || templateConflist.size() == 0) {
            logAppender.append(
              LogUtils.generateInfo(
                s"Can not get any template conf data with template name:$templateName\n"
              )
            )
          } else {
            // to remove metedata start param
            TaskUtils.clearStartupMap(params)

            if (EntranceConfiguration.TEMPLATE_CONF_ADD_ONCE_LABEL_ENABLE.getValue) {
              val onceLabel =
                LabelBuilderFactoryContext.getLabelBuilderFactory.createLabel(
                  classOf[ExecuteOnceLabel]
                )
              logger.info("Add once label for task id:{}", requestPersistTask.getId.toString)
              requestPersistTask.getLabels.add(onceLabel)
            }
          }
        }

        // 针对aisql处理模板参数
        val isAisql = LANGUAGE_TYPE_AI_SQL.equals(codeType)
        if (
            isAisql && runtimeTemplateFlag && templateConflist != null && templateConflist
              .size() > 0
        ) {
          logger.info("aisql deal with template in runtime params.")
          logAppender.append(
            LogUtils.generateInfo(
              s"If task execution fails, the template $templateName configuration parameters will be used to rerun the task\n"
            )
          )
          val keyList = new util.HashMap[String, AnyRef]()
          templateConflist.asScala.foreach(ele => {
            keyList.put(ele.getKey, ele.getConfigValue)
          })
          val confRuntimeMap = new util.HashMap[String, AnyRef]()
          confRuntimeMap.put(LabelKeyConstant.TEMPLATE_CONF_NAME_KEY, keyList)
          // 缓存配置到runtime
          TaskUtils.addRuntimeMap(params, confRuntimeMap)
          // 如果是aisql则不需要手动处理模板参数
          templateConflist.clear()
        }

        if (templateConflist != null && templateConflist.size() > 0) {
          val keyList = new util.HashMap[String, AnyRef]()
          templateConflist.asScala.foreach(ele => {
            val key = ele.getKey
            val oldValue = startMap.get(key)
            if (oldValue != null && StringUtils.isNotBlank(oldValue.toString)) {
              logger.info(s"key:$key value:$oldValue not empty, skip to deal")
            } else {
              val newValue = ele.getConfigValue
              logger.info(s"key:$key value:$newValue will add to startMap params")
              if (TaskUtils.isWithDebugInfo(params)) {
                logAppender.append(LogUtils.generateInfo(s"add $key=$newValue\n"))
              }
              keyList.put(key, newValue)
            }
          })
          if (keyList.size() > 0) {
            logger.info(s"use template conf for templateName: ${templateName}")
            keyList.put(confTemplateNameKey, templateName)
            logAppender.append(
              LogUtils
                .generateInfo(s"use template conf with templateName: ${templateName} \n")
            )
            TaskUtils.addStartupMap(params, keyList)
          }
        } else if (!isAisql) {
          EntranceUtils.dealsparkDynamicConf(jobRequest, logAppender, jobRequest.getParams)
        }
      case _ =>
    }
    jobRequest
  }

}
