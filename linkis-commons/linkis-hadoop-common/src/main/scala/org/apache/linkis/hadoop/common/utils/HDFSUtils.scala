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
import org.apache.linkis.common.utils.{AESUtils, Logging, Utils}
import org.apache.linkis.hadoop.common.conf.HadoopConf
import org.apache.linkis.hadoop.common.conf.HadoopConf._
import org.apache.linkis.hadoop.common.entity.HDFSFileSystemContainer

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
    conf
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
    logger.info(s"user ${userName} to create Fs, create time ${createCount}")
    getUserGroupInformation(userName, label)
      .doAs(new PrivilegedExceptionAction[FileSystem] {
        def run: FileSystem = FileSystem.newInstance(conf)
      })
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
            logger.info(
              s"user${hdfsFileSystemContainer.getUser} to Force remove hdfsFileSystemContainer"
            )
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
    }

  def getUserGroupInformation(userName: String): UserGroupInformation = {
    getUserGroupInformation(userName, null);
  }

  def getUserGroupInformation(userName: String, label: String): UserGroupInformation = {
    if (isKerberosEnabled(label)) {
      if (!isKeytabProxyUserEnabled(label)) {
        val path = getLinkisUserKeytabFile(userName, label)
        val user = getKerberosUser(userName, label)
        UserGroupInformation.setConfiguration(getConfigurationByLabel(userName, label))
        UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
      } else {
        val superUser = getKeytabSuperUser(label)
        val path = getLinkisUserKeytabFile(superUser, label)
        val user = getKerberosUser(superUser, label)
        UserGroupInformation.setConfiguration(getConfigurationByLabel(superUser, label))
        UserGroupInformation.createProxyUser(
          userName,
          UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
        )
      }
    } else {
      UserGroupInformation.createRemoteUser(userName)
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
      // 读取文件
      val byte = Files.readAllBytes(Paths.get(getLinkisKeytabPath(label), userName + KEYTAB_SUFFIX))
      // 加密内容// 加密内容
      val encryptedContent = AESUtils.decrypt(byte, AESUtils.PASSWORD)
      val tempFile = Files.createTempFile(userName, KEYTAB_SUFFIX)
      Files.setPosixFilePermissions(tempFile, PosixFilePermissions.fromString("rw-------"))
      Files.write(tempFile, encryptedContent)
      tempFile.toString
    } else {
      new File(getKeytabPath(label), userName + KEYTAB_SUFFIX).getPath
    }
    path
  }

}
