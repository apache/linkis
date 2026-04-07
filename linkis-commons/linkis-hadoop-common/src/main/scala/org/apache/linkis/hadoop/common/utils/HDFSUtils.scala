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

package org.apache.linkis.hadoop.common.utils

import org.apache.linkis.common.conf.Configuration.LINKIS_KEYTAB_SWITCH
import org.apache.linkis.common.utils.{AESUtils, ByteTimeUtils, Logging, Utils}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.hadoop.common.conf.HadoopConf._
import org.apache.linkis.hadoop.common.entity.HDFSFileSystemContainer

import com.google.common.cache.{CacheBuilder, LoadingCache, RemovalCause, RemovalListener, RemovalNotification}
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.security.UserGroupInformation

import java.io.File
import java.nio.file.{Files, Paths}
import java.nio.file.attribute.PosixFilePermissions
import java.security.PrivilegedExceptionAction
import java.util.Base64
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConverters._

object HDFSUtils extends Logging {

  private val fileSystemCache: java.util.Map[String, HDFSFileSystemContainer] =
    new ConcurrentHashMap[String, HDFSFileSystemContainer]()

  // 缓存keytab文件路径，避免重复创建临时文件导致KeyTab对象内存泄漏
  private val keytabTempFileCache: LoadingCache[String, String] = {
    val removalListener = new RemovalListener[String, String] {
      override def onRemoval(notification: RemovalNotification[String, String]): Unit = {
        val key = notification.getKey
        val path = notification.getValue
        val cause = notification.getCause
        
        logger.info(s"Keytab cache entry removed: $key, cause: $cause")
        
        // 当缓存项被移除时，清理对应的临时文件
        if (path != null) {
          val file = new File(path)
          if (file.exists()) {
            if (file.delete()) {
              logger.info(s"Removed keytab temp file: $path")
            } else {
              logger.warn(s"Failed to remove keytab temp file: $path")
            }
          }
        }
      }
    }

    CacheBuilder.newBuilder()
      .maximumSize(1000) // 最大缓存项数量
      .expireAfterAccess(24, TimeUnit.HOURS) // 24小时未访问过期
      .removalListener(removalListener)
      .build(new com.google.common.cache.CacheLoader[String, String] {
        override def load(key: String): String = {
          // 这里不应该被调用，因为我们总是在put之前检查缓存
          throw new UnsupportedOperationException("Cache loader not supported")
        }
      })
  }

  private val LOCKER_SUFFIX = "_HDFS"
  private val DEFAULT_CACHE_LABEL = "default"
  private val JOINT = "_"
  val KEYTAB_SUFFIX = ".keytab"

  private val count = new AtomicLong

  /**
   * For FS opened with public tenants, we should not perform close action, but should close only
   * when hdfsfilesystem encounters closed problem
   * 对于使用公共租户开启的FS，我们不应该去执行close动作，应该由hdfsfilesystem遇到closed问题时才进行关闭
   */
  if (HadoopConf.HDFS_ENABLE_CACHE && HadoopConf.HDFS_ENABLE_CACHE_CLOSE) {
    logger.info("HDFS Cache clear enabled ")
    Utils.defaultScheduler.scheduleAtFixedRate(
      new Runnable {
        override def run(): Unit = Utils.tryAndWarn {
          fileSystemCache
            .values()
            .asScala
            .filter { hdfsFileSystemContainer =>
              hdfsFileSystemContainer.canRemove() && StringUtils.isNotBlank(
                hdfsFileSystemContainer.getUser
              )
            }
            .foreach { hdfsFileSystemContainer =>
              val locker = hdfsFileSystemContainer.getUser + LOCKER_SUFFIX
              locker.intern() synchronized {
                if (
                    hdfsFileSystemContainer.canRemove() && !HadoopConf.HDFS_ENABLE_NOT_CLOSE_USERS
                      .contains(hdfsFileSystemContainer.getUser)
                ) {
                  fileSystemCache.remove(
                    hdfsFileSystemContainer.getUser + JOINT + hdfsFileSystemContainer.getLabel
                  )
                  IOUtils.closeQuietly(hdfsFileSystemContainer.getFileSystem)
                  logger.info(
                    s"user${hdfsFileSystemContainer.getUser} to remove hdfsFileSystemContainer,because hdfsFileSystemContainer can remove"
                  )
                }
              }
            }
        }
      },
      3 * 60 * 1000,
      60 * 1000,
      TimeUnit.MILLISECONDS
    )
  }

  /**
   * 创建 keytab 缓存的 key，考虑 label 参数
   */
  private def createKeytabCacheKey(userName: String, label: String): String = {
    if (label == null) userName else s"$userName#$label"
  }

  /**
   * 获取 keytab 临时文件目录
   */
  private def getKeytabTempDir(): java.nio.file.Path = {
    Paths.get(HadoopConf.KEYTAB_TEMP_DIR.getValue)
  }

  def getConfiguration(user: String): Configuration = getConfiguration(user, hadoopConfDir)

  def getConfigurationByLabel(user: String, label: String): Configuration = {
    getConfiguration(user, getHadoopConDirByLabel(label))
  }

  private def getHadoopConDirByLabel(label: String): String = {
    if (StringUtils.isBlank(label)) {
      hadoopConfDir
    } else {
      val prefix = if (HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue.endsWith("/")) {
        HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue
      } else {
        HadoopConf.HADOOP_EXTERNAL_CONF_DIR_PREFIX.getValue + "/"
      }
      prefix + label
    }
  }

  def getConfiguration(user: String, hadoopConfDir: String): Configuration = {
    val startTime = System.currentTimeMillis()
    logger.info(s"Loading Hadoop configuration - user: $user, configDir: $hadoopConfDir")
    try {
      val confPath = new File(hadoopConfDir)
      if (!confPath.exists() || confPath.isFile) {
        throw new RuntimeException(
          s"Create hadoop configuration failed, path $hadoopConfDir not exists."
        )
      }
      val conf = new Configuration()
      conf.addResource(
        new Path(Paths.get(hadoopConfDir, "core-site.xml").toAbsolutePath.toFile.getAbsolutePath)
      )
      conf.addResource(
        new Path(Paths.get(hadoopConfDir, "hdfs-site.xml").toAbsolutePath.toFile.getAbsolutePath)
      )
      val duration = System.currentTimeMillis() - startTime
      logger.info(
        s"Hadoop configuration loaded successfully - user: $user, configDir: $hadoopConfDir, duration: ${ByteTimeUtils
          .msDurationToString(duration)}"
      )
      conf
    } catch {
      case e: Exception =>
        val duration = System.currentTimeMillis() - startTime
        logger.error(
          s"Failed to load Hadoop configuration - user: $user, configDir: $hadoopConfDir, duration: ${ByteTimeUtils
            .msDurationToString(duration)}",
          e
        )
        throw e
    }
  }

  def getHDFSRootUserFileSystem: FileSystem = getHDFSRootUserFileSystem(
    getConfiguration(HADOOP_ROOT_USER.getValue)
  )

  def getHDFSRootUserFileSystem(conf: org.apache.hadoop.conf.Configuration): FileSystem =
    getHDFSUserFileSystem(HADOOP_ROOT_USER.getValue, null, conf)

  /**
   * If the cache switch is turned on, fs will be obtained from the cache first
   * @param userName
   * @return
   */
  def getHDFSUserFileSystem(userName: String): FileSystem = {
    getHDFSUserFileSystem(userName, null)
  }

  def getHDFSUserFileSystem(userName: String, label: String): FileSystem = {

    if (HadoopConf.HDFS_ENABLE_CACHE) {
      val cacheLabel = if (label == null) DEFAULT_CACHE_LABEL else label
      val cacheKey = userName + JOINT + cacheLabel
      val locker = userName + LOCKER_SUFFIX
      locker.intern().synchronized {
        if (fileSystemCache.containsKey(cacheKey)) {
          val hdfsFileSystemContainer = fileSystemCache.get(cacheKey)
          hdfsFileSystemContainer.addAccessCount()
          hdfsFileSystemContainer.updateLastAccessTime
          hdfsFileSystemContainer.getFileSystem
        } else {
          getHDFSUserFileSystem(userName, label, getConfigurationByLabel(userName, label))
        }
      }
    } else {
      getHDFSUserFileSystem(userName, label, getConfigurationByLabel(userName, label))
    }
  }

  def getHDFSUserFileSystem(
      userName: String,
      label: String,
      conf: org.apache.hadoop.conf.Configuration
  ): FileSystem = {

    if (HadoopConf.FS_CACHE_DISABLE.getValue && null != conf) {
      conf.set("fs.hdfs.impl.disable.cache", "true")
    }
    if (HadoopConf.HDFS_ENABLE_CACHE) {
      val locker = userName + LOCKER_SUFFIX
      val cacheLabel = if (label == null) DEFAULT_CACHE_LABEL else label
      val cacheKey = userName + JOINT + cacheLabel
      locker.intern().synchronized {
        val hdfsFileSystemContainer = if (fileSystemCache.containsKey(cacheKey)) {
          fileSystemCache.get(cacheKey)
        } else {
          // we use cacheLabel to create HDFSFileSystemContainer, and in the rest part of HDFSUtils, we consistently
          // use the same cacheLabel to operate HDFSFileSystemContainer, like close or remove.
          // At the same time, we don't want to change the behavior of createFileSystem which is out of HDFSUtils,
          // so we continue to use the original label to createFileSystem.
          val newHDFSFileSystemContainer =
            new HDFSFileSystemContainer(
              createFileSystem(userName, label, conf),
              userName,
              cacheLabel
            )
          fileSystemCache.put(cacheKey, newHDFSFileSystemContainer)
          newHDFSFileSystemContainer
        }
        hdfsFileSystemContainer.addAccessCount()
        hdfsFileSystemContainer.updateLastAccessTime
        hdfsFileSystemContainer.getFileSystem
      }
    } else {
      createFileSystem(userName, label, conf)
    }
  }

  def createFileSystem(userName: String, conf: org.apache.hadoop.conf.Configuration): FileSystem =
    createFileSystem(userName, null, conf)

  def createFileSystem(
      userName: String,
      label: String,
      conf: org.apache.hadoop.conf.Configuration
  ): FileSystem = {
    val createCount = count.getAndIncrement()
    val startTime = System.currentTimeMillis()
    val labelInfo = if (label == null) "default" else label
    logger.info(
      s"Creating Hadoop FileSystem - user: $userName, label: $labelInfo, createCount: $createCount"
    )
    try {
      val fs = getUserGroupInformation(userName, label)
        .doAs(new PrivilegedExceptionAction[FileSystem] {
          def run: FileSystem = FileSystem.newInstance(conf)
        })
      val duration = System.currentTimeMillis() - startTime
      logger.info(
        s"Hadoop FileSystem created successfully - user: $userName, label: $labelInfo, duration: ${ByteTimeUtils
          .msDurationToString(duration)}, createCount: $createCount"
      )
      fs
    } catch {
      case e: Exception =>
        val duration = System.currentTimeMillis() - startTime
        logger.error(
          s"Failed to create Hadoop FileSystem - user: $userName, label: $labelInfo, duration: ${ByteTimeUtils
            .msDurationToString(duration)}, createCount: $createCount",
          e
        )
        throw e
    }
  }

  def closeHDFSFIleSystem(fileSystem: FileSystem, userName: String): Unit =
    if (null != fileSystem && StringUtils.isNotBlank(userName)) {
      closeHDFSFIleSystem(fileSystem, userName, null, false)
    }

  def closeHDFSFIleSystem(fileSystem: FileSystem, userName: String, label: String): Unit =
    closeHDFSFIleSystem(fileSystem, userName, label, false)

  def closeHDFSFIleSystem(fileSystem: FileSystem, userName: String, isForce: Boolean): Unit =
    closeHDFSFIleSystem(fileSystem, userName, null, isForce)

  def closeHDFSFIleSystem(
      fileSystem: FileSystem,
      userName: String,
      label: String,
      isForce: Boolean
  ): Unit =
    if (null != fileSystem && StringUtils.isNotBlank(userName)) {
      val startTime = System.currentTimeMillis()
      val labelInfo = if (label == null) "default" else label
      logger.info(
        s"Closing Hadoop FileSystem - user: $userName, label: $labelInfo, force: $isForce"
      )
      try {
        val locker = userName + LOCKER_SUFFIX
        if (HadoopConf.HDFS_ENABLE_CACHE) locker.intern().synchronized {
          val cacheLabel = if (label == null) DEFAULT_CACHE_LABEL else label
          val cacheKey = userName + JOINT + cacheLabel
          val hdfsFileSystemContainer = fileSystemCache.get(cacheKey)
          if (
              null != hdfsFileSystemContainer && fileSystem == hdfsFileSystemContainer.getFileSystem
          ) {
            if (isForce) {
              fileSystemCache.remove(hdfsFileSystemContainer.getUser)
              IOUtils.closeQuietly(hdfsFileSystemContainer.getFileSystem)
              logger.info(s"Force closed Hadoop FileSystem - user: $userName, label: $labelInfo")
            } else {
              hdfsFileSystemContainer.minusAccessCount()
            }
          } else {
            IOUtils.closeQuietly(fileSystem)
          }
        }
        else {
          IOUtils.closeQuietly(fileSystem)
        }
        val duration = System.currentTimeMillis() - startTime
        logger.info(
          s"Hadoop FileSystem closed successfully - user: $userName, label: $labelInfo, duration: ${ByteTimeUtils
            .msDurationToString(duration)}"
        )
      } catch {
        case e: Exception =>
          val duration = System.currentTimeMillis() - startTime
          logger.error(
            s"Failed to close Hadoop FileSystem - user: $userName, label: $labelInfo, duration: ${ByteTimeUtils
              .msDurationToString(duration)}",
            e
          )
          throw e
      }
    }

  def getUserGroupInformation(userName: String): UserGroupInformation = {
    getUserGroupInformation(userName, null);
  }

  def getUserGroupInformation(userName: String, label: String): UserGroupInformation = {
    val startTime = System.currentTimeMillis()
    val labelInfo = if (label == null) "default" else label
    val authMethod = if (isKerberosEnabled(label)) "kerberos" else "simple"
    logger.info(
      s"Getting UserGroupInformation - user: $userName, label: $labelInfo, authMethod: $authMethod"
    )
    try {
      val ugi = if (isKerberosEnabled(label)) {
        if (!isKeytabProxyUserEnabled(label)) {
          val path = getLinkisUserKeytabFile(userName, label)
          val user = getKerberosUser(userName, label)
          logger.info(s"Performing Kerberos login with keytab - user: $userName, label: $labelInfo")
          UserGroupInformation.setConfiguration(getConfigurationByLabel(userName, label))
          UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
        } else {
          val superUser = getKeytabSuperUser(label)
          val path = getLinkisUserKeytabFile(superUser, label)
          val user = getKerberosUser(superUser, label)
          logger.info(
            s"Performing Kerberos login with proxy user - user: $userName, superUser: $superUser, label: $labelInfo"
          )
          UserGroupInformation.setConfiguration(getConfigurationByLabel(superUser, label))
          UserGroupInformation.createProxyUser(
            userName,
            UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
          )
        }
      } else {
        UserGroupInformation.createRemoteUser(userName)
      }
      val duration = System.currentTimeMillis() - startTime
      logger.info(
        s"UserGroupInformation obtained successfully - user: $userName, label: $labelInfo, authMethod: $authMethod, duration: ${ByteTimeUtils
          .msDurationToString(duration)}"
      )
      ugi
    } catch {
      case e: Exception =>
        val duration = System.currentTimeMillis() - startTime
        logger.error(
          s"Failed to get UserGroupInformation - user: $userName, label: $labelInfo, authMethod: $authMethod, duration: ${ByteTimeUtils
            .msDurationToString(duration)}",
          e
        )
        throw e
    }
  }

  def isKerberosEnabled(label: String): Boolean = {
    if (label == null) {
      KERBEROS_ENABLE
    } else {
      kerberosValueMapParser(KERBEROS_ENABLE_MAP.getValue).get(label).contains("true")
    }
  }

  def isKeytabProxyUserEnabled(label: String): Boolean = {
    if (label == null) {
      KEYTAB_PROXYUSER_ENABLED.getValue
    } else {
      kerberosValueMapParser(KEYTAB_PROXYUSER_SUPERUSER_MAP.getValue).contains(label)
    }
  }

  def getKerberosUser(userName: String, label: String): String = {
    var user = userName
    if (label == null) {
      if (KEYTAB_HOST_ENABLED.getValue) {
        user = user + "/" + KEYTAB_HOST.getValue
      }
    } else {
      val hostMap = kerberosValueMapParser(KEYTAB_HOST_MAP.getValue)
      if (hostMap.contains(label)) {
        user = user + "/" + hostMap(label)
      }
    }
    user
  }

  def getKeytabSuperUser(label: String): String = {
    if (label == null) {
      KEYTAB_PROXYUSER_SUPERUSER.getValue
    } else {
      kerberosValueMapParser(KEYTAB_PROXYUSER_SUPERUSER_MAP.getValue)(label)
    }
  }

  def getKeytabPath(label: String): String = {
    if (label == null) {
      KEYTAB_FILE.getValue
    } else {
      val prefix = if (EXTERNAL_KEYTAB_FILE_PREFIX.getValue.endsWith("/")) {
        EXTERNAL_KEYTAB_FILE_PREFIX.getValue
      } else {
        EXTERNAL_KEYTAB_FILE_PREFIX.getValue + "/"
      }
      prefix + label
    }
  }

  def getLinkisKeytabPath(label: String): String = {
    if (label == null) {
      LINKIS_KEYTAB_FILE.getValue
    } else {
      val prefix = if (EXTERNAL_KEYTAB_FILE_PREFIX.getValue.endsWith("/")) {
        EXTERNAL_KEYTAB_FILE_PREFIX.getValue
      } else {
        EXTERNAL_KEYTAB_FILE_PREFIX.getValue + "/"
      }
      prefix + label
    }
  }

  private def kerberosValueMapParser(configV: String): Map[String, String] = {
    val confDelimiter = ","
    if (configV == null || "".equals(configV)) {
      Map()
    } else {
      configV
        .split(confDelimiter)
        .filter(x => x != null && !"".equals(x))
        .map(x => {
          val confArr = x.split("=")
          if (confArr.length == 2) {
            (confArr(0).trim, confArr(1).trim)
          } else null
        })
        .filter(kerberosValue =>
          kerberosValue != null && StringUtils.isNotBlank(
            kerberosValue._1
          ) && null != kerberosValue._2
        )
        .toMap
    }
  }

  private def getLinkisUserKeytabFile(userName: String, label: String): String = {
    val path = if (LINKIS_KEYTAB_SWITCH) {
      try {
        val cacheKey = createKeytabCacheKey(userName, label)
        val keytabTempDir = getKeytabTempDir()
        synchronized {
          // 确保keytab临时目录存在
          if (!Files.exists(keytabTempDir)) {
            Files.createDirectories(keytabTempDir)
            Files.setPosixFilePermissions(keytabTempDir, PosixFilePermissions.fromString("rwxr-xr-x"))
          }

          val cachedPath = keytabTempFileCache.getIfPresent(cacheKey)
          if (cachedPath != null) {
            val tempFile = new File(cachedPath)
            if (tempFile.exists()) {
              logger.info(s"Found cached keytab file: $cachedPath")
              cachedPath
            } else {
              logger.info(s"Cached keytab file not exists, removing from cache: $cachedPath")
              // 文件不存在，从缓存中移除
              keytabTempFileCache.invalidate(cacheKey)
              // 创建新的临时文件
              createNewKeytabFile(userName, label, keytabTempDir, cacheKey)
            }
          } else {
            logger.info(s"Creating new keytab file for cacheKey: $cacheKey")
            // 创建新的临时文件
            createNewKeytabFile(userName, label, keytabTempDir, cacheKey)
          }
        }
      } catch {
        case _: Throwable => new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
      }
    } else {
      new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
    }
    path
  }

  private def createNewKeytabFile(
      userName: String,
      label: String,
      keytabTempDir: java.nio.file.Path,
      cacheKey: String
  ): String = {
    try {
      // 读取文件
      val sourcePath = Paths.get(getLinkisKeytabPath(label), userName + KEYTAB_SUFFIX)
      val byte = Files.readAllBytes(sourcePath)
      // 解密内容
      val encryptedContent = AESUtils.decrypt(byte, AESUtils.PASSWORD)
      val tempFile = Files.createTempFile(keytabTempDir, null, KEYTAB_SUFFIX)
      Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"))
      Files.write(tempFile, encryptedContent)
      val keyTablePath = tempFile.toString
      // 将固定文件路径加入缓存
      keytabTempFileCache.put(cacheKey, keyTablePath)
      logger.info(s"Created and cached fixed keytab file: $keyTablePath, cacheKey: $cacheKey")
      keyTablePath
    } catch {
      case e: Exception =>
        logger.error(s"Failed to create keytab file for user: $userName", e)
        throw e
    }
  }

}
