package com.webank.wedatasphere.linkis.httpclient

import java.io.InputStream

import com.ning.http.multipart.PartSource
import com.webank.wedatasphere.linkis.common.io.Fs

/**
  * Created by johnnwang on 2019/12/12.
  */
class FsPartSource (fs: Fs, path: String) extends PartSource {
  val fsPath = fs.get(path)
  override def getLength: Long = fsPath.getLength

  override def createInputStream(): InputStream = fs.read(fsPath)

  override def getFileName: String = fsPath.toFile.getName
}
