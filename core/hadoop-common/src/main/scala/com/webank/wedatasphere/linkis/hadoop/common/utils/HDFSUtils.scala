package com.webank.wedatasphere.linkis.hadoop.common.utils

import java.io.File
import java.nio.file.Paths
import java.security.PrivilegedExceptionAction

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import com.webank.wedatasphere.linkis.hadoop.common.conf.HadoopConf._
import org.apache.hadoop.security.UserGroupInformation
import com.webank.wedatasphere.linkis.common.conf.Configuration.hadoopConfDir
/**
  * Created by enjoyyin on 2019/5/27.
  */
object HDFSUtils {



  def getConfiguration(user: String): Configuration = getConfiguration(user, hadoopConfDir)

  def getConfiguration(user: String, hadoopConfDir: String): Configuration = {
    val confPath = new File(hadoopConfDir)
    if(!confPath.exists() || confPath.isFile) {
      throw new RuntimeException(s"Create hadoop configuration failed, path $hadoopConfDir not exists.")
    }
    val conf = new Configuration()
    conf.addResource(new Path(Paths.get(hadoopConfDir, "core-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    conf.addResource(new Path(Paths.get(hadoopConfDir, "hdfs-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    conf.addResource(new Path(Paths.get(hadoopConfDir, "yarn-site.xml").toAbsolutePath.toFile.getAbsolutePath))
    conf
  }

  def getHDFSRootUserFileSystem: FileSystem = getHDFSRootUserFileSystem(getConfiguration(HADOOP_ROOT_USER.getValue))

  def getHDFSRootUserFileSystem(conf: org.apache.hadoop.conf.Configuration): FileSystem =
    getHDFSUserFileSystem(HADOOP_ROOT_USER.getValue, conf)

  def getHDFSUserFileSystem(userName: String): FileSystem = getHDFSUserFileSystem(userName, getConfiguration(userName))

  def getHDFSUserFileSystem(userName: String, conf: org.apache.hadoop.conf.Configuration): FileSystem =
    getUserGroupInformation(userName)
      .doAs(new PrivilegedExceptionAction[FileSystem]{
        def run = FileSystem.get(conf)
      })
  def getUserGroupInformation(userName: String): UserGroupInformation ={
    if(KERBEROS_ENABLE.getValue) {
      val path = new File(KEYTAB_FILE.getValue , userName + ".keytab").getPath
      val user = getKerberosUser(userName)
      UserGroupInformation.setConfiguration(getConfiguration(userName))
      UserGroupInformation.loginUserFromKeytabAndReturnUGI(user, path)
    } else {
      UserGroupInformation.createRemoteUser(userName)
    }
  }

  def getKerberosUser(userName: String): String = {
    var user = userName
    if(KEYTAB_HOST_ENABLED.getValue){
      user = user+ "/" + kEYTAB_HOST.getValue
    }
    user
  }

}
