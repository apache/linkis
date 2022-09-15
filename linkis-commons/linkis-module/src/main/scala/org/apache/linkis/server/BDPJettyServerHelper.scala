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

package org.apache.linkis.server

import org.apache.linkis.DataWorkCloudApplication
import org.apache.linkis.common.utils.Logging
import org.apache.linkis.server.conf.ServerConfiguration._
import org.apache.linkis.server.socket.ControllerServer
import org.apache.linkis.server.socket.controller.{ServerEventService, ServerListenerEventBus}

import org.apache.commons.io.FileUtils

import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.servlet.DispatcherServlet

import javax.servlet.{DispatcherType, Filter, MultipartConfigElement}

import java.io.File
import java.lang
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.EnumSet

import scala.collection.mutable

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson._
import org.eclipse.jetty.server.session.SessionHandler
import org.eclipse.jetty.servlet.{
  DefaultServlet,
  FilterHolder,
  ServletContextHandler,
  ServletHolder
}
import org.eclipse.jetty.webapp.WebAppContext

private[linkis] object BDPJettyServerHelper extends Logging {

  private var serverListenerEventBus: ServerListenerEventBus = _
  private var controllerServer: ControllerServer = _
  private val services = mutable.Buffer[ServerEventService]()

  private val TMP_FOLDER = ""
  private val MAX_UPLOAD_SIZE = 200 * 1024 * 1024

  private[server] def getControllerServer = controllerServer

  private def createServerListenerEventBus(): Unit = {
    serverListenerEventBus = new ServerListenerEventBus(
      BDP_SERVER_EVENT_QUEUE_SIZE.getValue,
      "WebSocket-Server-Event-ListenerBus",
      BDP_SERVER_EVENT_CONSUMER_THREAD_SIZE.getValue,
      BDP_SERVER_EVENT_CONSUMER_THREAD_FREE_MAX.getValue.toLong
    )
    services.foreach(serverListenerEventBus.addListener)
    serverListenerEventBus.start()
  }

  def addServerEventService(serverEventService: ServerEventService): Unit = {
    if (serverListenerEventBus != null) serverListenerEventBus.addListener(serverEventService)
    else services += serverEventService
  }

  private def getSecurityFilter(): Class[Filter] =
    Class.forName(BDP_SERVER_SECURITY_FILTER.getValue).asInstanceOf[Class[Filter]]

  def setupSpringRestApiContextHandler(webApp: ServletContextHandler) {
    val context = new AnnotationConfigWebApplicationContext
    // val CONFIG_LOCATION = "org.apache.linkis.manager.am"
    val CONFIG_LOCATION = ""
    context.setConfigLocation(CONFIG_LOCATION);
    val serlvet = new DispatcherServlet(context)
    val servletHolder = new ServletHolder(serlvet)

    servletHolder.setName("springrestful")
    servletHolder.setForcedPath("springrestful")

    // todo  file size  parameter configuration
    // val multipartConfigElement = new MultipartConfigElement(null, MAX_UPLOAD_SIZE, MAX_UPLOAD_SIZE * 2, MAX_UPLOAD_SIZE / 2)
    val multipartConfigElement =
      DataWorkCloudApplication.getApplicationContext.getBean(classOf[MultipartConfigElement]);
    servletHolder.getRegistration.setMultipartConfig(multipartConfigElement)

    val p = BDP_SERVER_RESTFUL_URI.getValue
    val restfulPath =
      if (p.endsWith("/*")) p
      else if (p.endsWith("/")) p + "*"
      else p + "/*"

    webApp.addServlet(servletHolder, restfulPath)
    val filterHolder = new FilterHolder(getSecurityFilter())
    webApp.addFilter(filterHolder, restfulPath, EnumSet.allOf(classOf[DispatcherType]))
    webApp.setSessionHandler(new SessionHandler)

  }

  def setupControllerServer(webApp: ServletContextHandler): ControllerServer = {
    if (controllerServer != null) return controllerServer
    synchronized {
      if (controllerServer != null) return controllerServer
      createServerListenerEventBus()
      controllerServer = new ControllerServer(serverListenerEventBus)
      val maxTextMessageSize = BDP_SERVER_SOCKET_TEXT_MESSAGE_SIZE_MAX.getValue
      val servletHolder = new ServletHolder(controllerServer)
      servletHolder.setInitParameter("maxTextMessageSize", maxTextMessageSize)
      val p = BDP_SERVER_SOCKET_URI.getValue
      val socketPath =
        if (p.endsWith("/*")) p
        else if (p.endsWith("/")) p + "*"
        else p + "/*"
      webApp.addServlet(servletHolder, socketPath)
      controllerServer
    }
  }

  def setupWebAppContext(webApp: WebAppContext): Unit = {
    webApp.setContextPath(BDP_SERVER_SERVER_CONTEXT_PATH.getValue)
    var warPath = new File(BDP_SERVER_WAR.getValue)
    warPath.listFiles().find(_.getName.endsWith(".war")).foreach(warPath = _)
    if (warPath.isDirectory) {
      // Development mode, read from FS
      // webApp.setDescriptor(warPath+"/WEB-INF/web.xml");
      webApp.setResourceBase(warPath.getPath)
      webApp.setParentLoaderPriority(true)
    } else {
      // use packaged WAR
      webApp.setWar(warPath.getAbsolutePath)
      webApp.setExtractWAR(true)
      val warTempDirectory = new File(BDP_SERVER_WAR_TEMPDIR.getValue)
      if (warTempDirectory.exists) {
        logger.warn(s"delete ${warTempDirectory.getPath}, since it is exists.")
        FileUtils.deleteDirectory(warTempDirectory)
      }
      warTempDirectory.mkdir
      logger.info(s"BDPJettyServer Webapps path: ${warTempDirectory.getPath}.")
      webApp.setTempDirectory(warTempDirectory)
    }
    // Explicit bind to root
    webApp.addServlet(new ServletHolder(new DefaultServlet), "/*")
    webApp.setWelcomeFiles(Array("index.html", "index.htm"))
    webApp.getSessionHandler.setMaxInactiveInterval(
      (BDP_SERVER_WEB_SESSION_TIMEOUT.getValue.toLong / 1000).toInt
    )
    webApp.setInitParameter(
      "org.eclipse.jetty.servlet.Default.dirAllowed",
      BDP_SERVER_SERVER_DEFAULT_DIR_ALLOWED.getValue
    )
  }

  implicit val gson = new GsonBuilder()
    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
    .serializeNulls
    .registerTypeAdapter(
      classOf[java.lang.Double],
      new JsonSerializer[java.lang.Double] {

        override def serialize(
            t: lang.Double,
            `type`: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement =
          if (t == t.longValue()) new JsonPrimitive(t.longValue()) else new JsonPrimitive(t)

      }
    )
    .create

  implicit val jacksonJson =
    new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))

}
