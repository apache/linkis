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

package org.apache.linkis.common.utils

import org.apache.linkis.common.io.{Fs, FsPath}

import org.apache.commons.io.IOUtils

import java.io._
import java.util.zip.{ZipEntry, ZipInputStream, ZipOutputStream}

import scala.collection.JavaConverters._

object ZipUtils {

  private val BUFFER_SIZE = 4096

  /**
   * The source file stored in the sourceFilePath directory is packaged into a zip file with the
   * fileName name and stored in the zipFilePath directory.
   * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
   * @param sourceFilePath
   *   :File path to be compressed（待压缩的文件路径）
   * @param zipFilePath
   *   :Compressed storage path（压缩后存放路径）
   * @param fileName
   *   :The name of the compressed file（压缩后文件的名称）
   * @return
   */
  def zip(sourceFilePath: String, zipFilePath: String, fileName: String)(implicit fs: Fs): Unit = {
    var bis: BufferedInputStream = null
    var zos: ZipOutputStream = null
    val sourcePath = new FsPath(sourceFilePath)
    if (!fs.exists(sourcePath)) {
      throw new IOException(
        "File directory to be compressed（待压缩的文件目录）：" + sourceFilePath + "does not exist（不存在）."
      )
    }
    val zipFile = FsPath.getFsPath(zipFilePath, fileName)
    if (fs.exists(zipFile)) {
      throw new IOException(
        zipFilePath + "The name exists under the directory（目录下存在名字为）:" + fileName + "Package file（打包文件）."
      )
    }
    val sourceFiles = fs.list(sourcePath)
    if (null == sourceFiles || sourceFiles.size() < 1) {
      throw new IOException(
        "File directory to be compressed（待压缩的文件目录）：" + sourceFilePath + "There are no files in it, no need to compress.(里面不存在文件，无需压缩.)"
      )
    }
    Utils.tryFinally {
      val fos = fs.write(zipFile, true)
      zos = new ZipOutputStream(new BufferedOutputStream(fos))
      val bufs = new Array[Byte](BUFFER_SIZE)
      sourceFiles.asScala.foreach { f =>
        // Create a ZIP entity and add it to the archive(创建ZIP实体，并添加进压缩包)
        val zipEntry = new ZipEntry(f.toFile.getName)
        zos.putNextEntry(zipEntry)
        // Read the file to be compressed and write it into the archive（读取待压缩的文件并写进压缩包里）
        val fis = new FileInputStream(f.toFile)
        bis = new BufferedInputStream(fis, 1024 * 10)
        Utils.tryFinally {
          var read = bis.read(bufs, 0, 1024)
          while (read > 0) {
            zos.write(bufs, 0, read)
            read = bis.read(bufs, 0, 1024)
          }
        }(IOUtils.closeQuietly(bis))
      }
    }(IOUtils.closeQuietly(zos))
  }

  @deprecated
  def zipDir(sourceFilePath: String, zipFilePath: String, fileName: String)(implicit
      fileService: FileService,
      user: String
  ): Unit = {
    var bis: BufferedInputStream = null
    var zos: ZipOutputStream = null
    if (!fileService.exists(sourceFilePath, user)) {
      throw new IOException(
        "File directory to be compressed（待压缩的文件目录）：" + sourceFilePath + "does not exist（不存在）."
      )
    }
    val zipFile = new File(zipFilePath, fileName)
    if (fileService.exists(zipFile.getAbsolutePath, user)) {
      throw new IOException(
        zipFilePath + "The name exists under the directory（目录下存在名字为）:" + fileName + "Package file（打包文件）."
      )
    } else fileService.createFile(zipFile.getAbsolutePath, user, true)
    val sourceFiles = fileService.listFileNames(sourceFilePath, user)
    if (null == sourceFiles || sourceFiles.length < 1) {
      throw new IOException(
        "File directory to be compressed(待压缩的文件目录)：" + sourceFilePath + "There are no files in it, no need to compress(里面不存在文件，无需压缩)."
      )
    }
    Utils.tryFinally {
      val fos = fileService.append(zipFile.getAbsolutePath, user, true)
      zos = new ZipOutputStream(new BufferedOutputStream(fos))
      val bufs = new Array[Byte](BUFFER_SIZE)
      sourceFiles.foreach { f =>
        // Create a ZIP entity and add it to the archive(创建ZIP实体，并添加进压缩包)
        val zipEntry = new ZipEntry(f)
        zos.putNextEntry(zipEntry)
        // Read the file to be compressed and write it into the archive(读取待压缩的文件并写进压缩包里)
        val fis = fileService.open(new File(sourceFilePath, f).getPath, user)
        bis = new BufferedInputStream(fis, 1024 * 10)
        Utils.tryFinally {
          var read = bis.read(bufs, 0, 1024)
          while (read > 0) {
            zos.write(bufs, 0, read)
            read = bis.read(bufs, 0, 1024)
          }
        }(IOUtils.closeQuietly(bis))
      }
    }(IOUtils.closeQuietly(zos))
  }

  /**
   * The source file stored in the sourceFilePath directory is packaged into a zip file with the
   * fileName name and stored in the zipFilePath directory.
   * 将存放在sourceFilePath目录下的源文件，打包成fileName名称的zip文件，并存放到zipFilePath路径下
   * @param sourceFilePath
   *   :File path to be compressed(待压缩的文件路径)
   * @param zipFilePath
   *   :Compressed storage path(压缩后存放路径)
   * @param fileName
   *   :The name of the compressed file(压缩后文件的名称)
   * @return
   */
  def fileToZip(sourceFilePath: String, zipFilePath: String, fileName: String): Unit = {
    val sourceFile = new File(sourceFilePath)
    var bis: BufferedInputStream = null
    var zos: ZipOutputStream = null
    if (!sourceFile.exists()) {
      throw new IOException(
        "File directory to be compressed(待压缩的文件目录)：" + sourceFilePath + "does not exist(不存在)."
      )
    }
    val zipFile = new File(zipFilePath + "/" + fileName)
    if (zipFile.exists()) {
      throw new IOException(
        zipFilePath + "The name exists under the directory(目录下存在名字为):" + fileName + "Package file(打包文件)."
      )
    }
    val sourceFiles = sourceFile.listFiles()
    if (null == sourceFiles || sourceFiles.length < 1) {
      throw new IOException(
        "File directory to be compressed(待压缩的文件目录)：" + sourceFilePath + "There are no files in it, no need to compress(里面不存在文件，无需压缩)."
      )
    }
    Utils.tryFinally {
      val fos = new FileOutputStream(zipFile)
      zos = new ZipOutputStream(new BufferedOutputStream(fos))
      write(
        sourceFilePath.substring(sourceFilePath.lastIndexOf(File.separator) + 1),
        sourceFile,
        "",
        zos
      )
    }(IOUtils.closeQuietly(zos))
  }

  private def write(
      rootPath: String,
      file: File,
      parentPath: String,
      zos: ZipOutputStream
  ): Unit = {
    if (file.isDirectory) { // 处理文件夹
      val _parentPath = parentPath + file.getName + File.separator
      val files = file.listFiles
      if (files.length != 0) for (f <- files) {
        write(rootPath, f, _parentPath, zos)
      }
      else { // 空目录则创建当前目录
        try {
          zos.putNextEntry(new ZipEntry(_parentPath))
        } catch {
          case e: IOException =>
            e.printStackTrace()
        }
      }
    } else {
      zos.putNextEntry(new ZipEntry(parentPath.substring(rootPath.length + 1) + file.getName))
      val fis = new FileInputStream(file)
      val bufs = new Array[Byte](BUFFER_SIZE)
      val bis = new BufferedInputStream(fis, 1024 * 10)
      Utils.tryFinally {
        var read = bis.read(bufs, 0, 1024)
        while (read > 0) {
          zos.write(bufs, 0, read)
          read = bis.read(bufs, 0, 1024)
        }
      }(IOUtils.closeQuietly(bis))
    }
  }

  @deprecated
  def unzipDir(zipFilePath: String, unzipDir: String)(implicit
      fileService: FileService,
      user: String
  ): Unit = {
    val zipIn = new ZipInputStream(fileService.open(zipFilePath, user))
    Utils.tryFinally {
      val destDir = new FsPath(unzipDir)
      if (!fileService.exists(unzipDir, user)) {
        fileService.mkdirs(unzipDir, user, true)
      }
      var entry = zipIn.getNextEntry
      while (entry != null) {
        var entryName = entry.getName
        if (entryName.contains(".." + File.separator)) {
          throw new IOException("Zip entry contains illegal characters: " + entryName)
        }
        val filePath = destDir.getPath + File.separator + entryName
        if (!entry.isDirectory) {
          extractPath(zipIn, filePath)
        } else {
          fileService.mkdirs(filePath, user, true)
        }
        zipIn.closeEntry()
        entry = zipIn.getNextEntry
      }
    }(IOUtils.closeQuietly(zipIn))
  }

  @deprecated
  private def extractPath(zipIn: ZipInputStream, destFilePath: String)(implicit
      fileService: FileService,
      user: String
  ) {
    val destFile = new File(destFilePath)
    if (!fileService.exists(destFile.getParent, user)) {
      fileService.mkdirs(destFile.getParent, user, true)
    }
    val bos = new BufferedOutputStream(fileService.append(destFilePath, user, true))
    Utils.tryFinally {
      // if file exists, replace it.
      // if file does NOT exist, copy it.
      val bytesIn = new Array[Byte](BUFFER_SIZE)
      var read = zipIn.read(bytesIn)
      while (read != -1) {
        bos.write(bytesIn, 0, read)
        read = zipIn.read(bytesIn)
      }
    }(IOUtils.closeQuietly(bos))
  }

  def unzip(zipFilePath: String, unzipDir: String)(implicit fs: Fs): Unit = {
    val zipIn = new ZipInputStream(fs.read(new FsPath(zipFilePath)))
    Utils.tryFinally {
      val destDir = new FsPath(unzipDir)
      if (!fs.exists(destDir)) {
        // TODO never goto here
        // fs.create("mkdirs " + unzipDir)
        fs.mkdir(new FsPath(unzipDir))
      }
      var entry = zipIn.getNextEntry
      while (entry != null) {
        var entryName = entry.getName
        if (entryName.contains(".." + File.separator)) {
          throw new IOException("Zip entry contains illegal characters: " + entryName)
        }
        val filePath = destDir.getPath + File.separator + entryName
        if (!entry.isDirectory) {
          extractFsPath(zipIn, filePath)
        } else {
          // TODO never goto here
          // fs.create("mkdir " + filePath)
          fs.mkdir(new FsPath(filePath))
        }
        zipIn.closeEntry()
        entry = zipIn.getNextEntry
      }
    }(IOUtils.closeQuietly(zipIn))
  }

  private def extractFsPath(zipIn: ZipInputStream, destFilePath: String)(implicit fs: Fs) {
    val destFile = new FsPath(destFilePath)
    if (!fs.exists(destFile.getParent)) {
      // fs.create("mkdir " + destFile.getParent.getPath)
      fs.mkdir(destFile.getParent)
    }
    val bos = new BufferedOutputStream(fs.write(destFile, true))
    Utils.tryFinally {
      // if file exists, replace it.
      // if file does NOT exist, copy it.
      val bytesIn = new Array[Byte](BUFFER_SIZE)
      var read = zipIn.read(bytesIn)
      while (read != -1) {
        bos.write(bytesIn, 0, read)
        read = zipIn.read(bytesIn)
      }
    }(IOUtils.closeQuietly(bos))
  }

  /**
   * @param zipFilePath
   *   Path of the zip file to be extracted
   * @param unzipDir
   *   Path of the destination directory
   */
  def fileToUnzip(zipFilePath: String, unzipDir: String): Unit = {
    val zipIn = new ZipInputStream(new FileInputStream(zipFilePath))
    Utils.tryFinally {
      val destDir = new File(unzipDir)
      if (!destDir.exists()) {
        destDir.mkdir()
      }
      var entry = zipIn.getNextEntry
      while (entry != null) {
        var entryName = entry.getName
        if (entryName.contains(".." + File.separator)) {
          throw new IOException("Zip entry contains illegal characters: " + entryName)
        }
        val filePath = destDir.getPath + File.separator + entryName
        if (!entry.isDirectory) {
          extractFile(zipIn, filePath)
        } else {
          val dir = new File(filePath)
          dir.mkdir
        }
        zipIn.closeEntry()
        entry = zipIn.getNextEntry
      }
    }(IOUtils.closeQuietly(zipIn))
  }

  /**
   * Extracts a single file
   * @param zipIn
   *   the ZipInputStream
   * @param destFilePath
   *   Path of the destination file
   */
  private def extractFile(zipIn: ZipInputStream, destFilePath: String) {
    val destFile = new File(destFilePath)
    if (!destFile.exists()) {
      destFile.getParentFile.mkdirs()
    }
    val bos = new BufferedOutputStream(new FileOutputStream(destFile))
    Utils.tryFinally {
      // if file exists, replace it.
      // if file does NOT exist, copy it.
      val bytesIn = new Array[Byte](BUFFER_SIZE)
      var read = zipIn.read(bytesIn)
      while (read != -1) {
        bos.write(bytesIn, 0, read)
        read = zipIn.read(bytesIn)
      }
    }(IOUtils.closeQuietly(bos))
  }

}
