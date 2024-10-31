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

package org.apache.linkis.filesystem.util;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.filesystem.exception.WorkspaceExceptionManager;
import org.apache.linkis.rpc.Sender;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.udf.api.rpc.RequestPythonInfo;
import org.apache.linkis.udf.api.rpc.ResponsePythonInfo;
import org.apache.linkis.udf.entity.PythonModuleInfoVO;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FilesystemUtils {

  private static final String[] OPERATORS = {"==", ">=", "<=", ">", "<", "~="};

  public static boolean checkFilePermissions(String filePermission) {
    boolean result = false;
    if (StringUtils.isNumeric(filePermission)) {
      char[] ps = filePermission.toCharArray();
      int ownerPermissions = Integer.parseInt(String.valueOf(ps[0]));
      if (ownerPermissions >= 4) {
        result = true;
      }
    }
    return result;
  }

  public static void traverseFolder(FsPath fsPath, FileSystem fileSystem, Stack<FsPath> dirsToChmod)
      throws IOException {
    List<FsPath> list = fileSystem.list(fsPath);
    if (list == null) {
      return;
    }
    for (FsPath path : list) {
      if (path.isdir()) {
        traverseFolder(path, fileSystem, dirsToChmod);
      }
      dirsToChmod.push(path);
    }
  }

  /**
   * 从 tar.gz 文件中查找PKG-INFO文件，并获取其需要打包的文件夹名称
   *
   * @param inputStream tar.gz 文件的输入流。
   * @return 包名，如果未找到则返回 null。
   * @throws IOException 如果文件读取失败。
   */
  public static String findPackageName(InputStream inputStream) throws IOException {
    int findPkgInfo = 0;
    try (TarArchiveInputStream tarInput =
        new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (entry.getName().endsWith("PKG-INFO")) {
          findPkgInfo = 1;
          String pkgInfoContent = IOUtils.toString(tarInput, StandardCharsets.UTF_8);
          return pkgInfoContent.split("Name: ")[1].split("\n")[0].trim();
        }
      }
    } catch (Exception e) {
      throw WorkspaceExceptionManager.createException(80039, e.getMessage());
    }
    if (findPkgInfo == 0) {
      throw WorkspaceExceptionManager.createException(80040, "PKG-INFO");
    }
    return null;
  }

  /**
   * 从 tar.gz 文件中获取指定文件夹的根路径。
   *
   * @param inputStream tar.gz 文件的输入流。
   * @param folder 指定的文件夹名称。
   * @return 文件夹的根路径，如果未找到则返回 null。
   * @throws IOException 如果文件读取失败。
   */
  private static String getRootPath(InputStream inputStream, String folder) throws IOException {
    try (TarArchiveInputStream tarInput =
        new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (entry.isDirectory()
            && entry.getName().endsWith(FsPath.SEPARATOR + folder + FsPath.SEPARATOR)) {
          return entry.getName();
        }
        if (entry.getName().contains(FsPath.SEPARATOR + folder + FsPath.SEPARATOR)) {
          String delimiter = FsPath.SEPARATOR + folder + FsPath.SEPARATOR;
          int delimiterIndex = entry.getName().indexOf(delimiter);
          return entry.getName().substring(0, delimiterIndex + delimiter.length());
        }
      }
    } catch (Exception e) {
      throw WorkspaceExceptionManager.createException(80039, e.getMessage());
    }
    return null;
  }

  /**
   * 将 tar.gz 文件中的指定文件夹内容转换为 zip 文件流。
   *
   * @param inputStream tar.gz 文件的输入流。
   * @param folder 指定的文件夹名称。
   * @param rootPath 文件夹的根路径。
   * @return 包含指定文件夹内容的 zip 文件流。
   * @throws IOException 如果文件读取或写入失败。
   */
  private static InputStream createZipFile(InputStream inputStream, String folder, String rootPath)
      throws IOException {
    TarArchiveInputStream tarInput =
        new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream);
    try {
      TarArchiveEntry entry;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (!entry.isDirectory()
            && entry.getName().contains(FsPath.SEPARATOR + folder + FsPath.SEPARATOR)) {
          // \dist\py_mysql-1.0.tar\py_mysql-1.0\py_mysql\lib\__init__.py
          ZipEntry zipEntry = new ZipEntry(entry.getName().substring(rootPath.length()));
          zos.putNextEntry(zipEntry);
          IOUtils.copy(tarInput, zos);
          zos.closeEntry();
        }
      }
    } catch (Exception e) {
      throw WorkspaceExceptionManager.createException(80039, e.getMessage());
    } finally {
      tarInput.close();
      zos.close();
    }
    // 将 ByteArrayOutputStream 转换为 InputStream
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  public static InputStream getZipInputStreamByTarInputStream(
      MultipartFile file, String packageName) throws IOException {
    String rootPath = getRootPath(file.getInputStream(), packageName);
    if (StringUtils.isBlank(rootPath)) {
      throw WorkspaceExceptionManager.createException(80038, packageName);
    }
    return createZipFile(file.getInputStream(), packageName, rootPath);
  }

  /**
   * * 检查python环境中模块是否存在，使用程序调用脚本实现 脚本保存在admin文件夹中，脚本名为check_python_module.py
   * 脚本执行存在返回ture，不存在返回false
   *
   * @param pythonModules 传入需要检查的模块列表
   * @param username
   * @return 返回不存在的模块列表
   */
  public static String checkModuleFile(List<String> pythonModules, String username)
      throws IOException {
    StringJoiner joiner = new StringJoiner(",");
    // 检查机器上pyhton环境中模块是否存在
    List<String> notExistModules =
        pythonModules.stream()
            .filter(
                module -> {
                  String exec =
                      Utils.exec(
                          (new String[] {
                            "python",
                            Configuration.getLinkisHome() + "/admin/" + "check_modules.py",
                            module
                          }));
                  return !Boolean.parseBoolean(exec);
                })
            .collect(Collectors.toList());
    // 查询数据库中包是否存在
    notExistModules.forEach(
        module -> {
          Object object =
              Sender.getSender("linkis-ps-publicservice")
                  .ask(new RequestPythonInfo(module, username));
          if (object instanceof ResponsePythonInfo) {
            ResponsePythonInfo response = (ResponsePythonInfo) object;
            PythonModuleInfoVO pythonModuleInfoVO = response.pythonModuleInfoVO();
            if (StringUtils.isBlank(pythonModuleInfoVO.getName())) {
              joiner.add(module);
            }
          }
        });
    return joiner.toString();
  }

  public static List<String> getInstallRequestPythonModules(MultipartFile file) throws IOException {
    List<String> modules = new ArrayList<>();
    String originalFilename = file.getOriginalFilename();
    if (StringUtils.isNotBlank(originalFilename) && originalFilename.endsWith(".tar.gz")) {
      int findSetup = 0;
      // 读取 setup.py 文件的内容，并使用正则表达式提取 install_requires 字段。
      // 解析 install_requires 字段中的依赖包信息
      try (TarArchiveInputStream tarInput =
          new TarArchiveInputStream(new GzipCompressorInputStream(file.getInputStream()))) {
        TarArchiveEntry entry;
        while ((entry = tarInput.getNextTarEntry()) != null) {
          if (entry.getName().endsWith("setup.py")) {
            findSetup = 1;
            String content = IOUtils.toString(tarInput, StandardCharsets.UTF_8);
            modules = extractDependencies(content);
            break;
          }
        }
      } catch (Exception e) {
        throw WorkspaceExceptionManager.createException(80039, e.getMessage());
      }
      if (findSetup == 0) {
        throw WorkspaceExceptionManager.createException(80040, "setup.py");
      }
    }
    return modules;
  }

  public static List<String> extractDependencies(String content) {
    List<String> modules = new ArrayList<>();
    Pattern pattern = Pattern.compile("install_requires=\\[(.*?)\\]", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(content);
    if (matcher.find()) {
      String requirements = matcher.group(1);
      String[] packages = requirements.split(",");
      for (String pkg : packages) {
        pkg = pkg.replaceAll("#.*?\\n", "").replaceAll("\\n", "").replaceAll("'", "").trim();
        for (String operator : OPERATORS) {
          if (pkg.contains(operator)) {
            String[] parts = pkg.split(operator);
            pkg = parts[0].trim();
          }
        }
        if (StringUtils.isNotBlank(pkg)) {
          modules.add(pkg);
        }
      }
    }
    return modules;
  }
}
