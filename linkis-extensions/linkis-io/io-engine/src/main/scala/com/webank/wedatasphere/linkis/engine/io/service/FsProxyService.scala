package com.webank.wedatasphere.linkis.engine.io.service

import com.webank.wedatasphere.linkis.common.utils.Logging
import com.webank.wedatasphere.linkis.storage.utils.{StorageConfiguration, StorageUtils}

/**
  * Created by johnnwang on 2019/1/29.
  */
class FsProxyService extends Logging{

  def canProxyUser(creatorUser:String, proxyUser:String, fsType:String): Boolean = creatorUser match {
    case StorageConfiguration.STORAGE_ROOT_USER.getValue => true
    case StorageConfiguration.LOCAL_ROOT_USER.getValue => StorageUtils.FILE == fsType
    case StorageConfiguration.HDFS_ROOT_USER.getValue => StorageUtils.HDFS == fsType
    case _ => creatorUser.equals(proxyUser)
  }
  /*  if(creatorUser.equals(proxyUser)){
     return true
    }
    if(creatorUser == StorageConfiguration.STORAGE_ROOT_USER.getValue ) return t
    if(StorageUtils.FILE == fsType && creatorUser == StorageConfiguration.LOCAL_ROOT_USER.getValue){
      return true
    }
    if(StorageUtils.HDFS == fsType && creatorUser == StorageConfiguration.HDFS_ROOT_USER.getValue){
      return true
    }
    info(s"$creatorUser Failed to proxy user:$proxyUser of FsType:$fsType")
     true*/

}
