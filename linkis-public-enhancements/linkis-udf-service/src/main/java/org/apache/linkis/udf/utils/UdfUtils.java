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

package org.apache.linkis.udf.utils;

import org.apache.linkis.common.conf.Configuration;
import org.apache.linkis.common.io.FsPath;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.udf.conf.Constants;
import org.apache.linkis.udf.exception.UdfException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdfUtils {
  private static final Logger logger = LoggerFactory.getLogger(UdfUtils.class);
  private static Set<String> moduleSet = new HashSet<>();

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
          return pkgInfoContent.split("Name: ")[1].split("\n")[0].trim().toLowerCase();
        }
      }
    } catch (Exception e) {

      throw new UdfException(80039, "File upload failed, error message:", e);
    }
    if (findPkgInfo == 0) {
      throw new UdfException(
          80040, "PKG-INFO file not found in the archive (PKG-INFO文件不存在，请确认包中包含PKG-INFO文件)");
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
          return entry.getName().replace(folder + FsPath.SEPARATOR, "");
        }
        if (entry.getName().contains(FsPath.SEPARATOR + folder + FsPath.SEPARATOR)) {
          String delimiter = FsPath.SEPARATOR + folder + FsPath.SEPARATOR;
          int delimiterIndex = entry.getName().indexOf(delimiter);
          return entry.getName().substring(0, delimiterIndex);
        }
      }
    } catch (Exception e) {
      throw new UdfException(80039, "File upload failed, error message:", e);
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
            && entry.getName().startsWith(rootPath)
            && entry.getName().contains(FsPath.SEPARATOR + folder + FsPath.SEPARATOR)) {
          // \dist\py_mysql-1.0.tar\py_mysql-1.0\py_mysql\lib\__init__.py
          ZipEntry zipEntry = new ZipEntry(entry.getName().substring(rootPath.length()));
          zos.putNextEntry(zipEntry);
          IOUtils.copy(tarInput, zos);
          zos.closeEntry();
        }
      }
    } catch (Exception e) {
      throw new UdfException(80039, "File upload failed, error message:", e);
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
      throw new UdfException(
          80038,
          "The name directory "
              + packageName
              + " specified by PKG-INFO does not exist. Please confirm that the "
              + packageName
              + " specified by PKG-INFO in the package matches the actual folder name (PKG-INFO指定Name目录"
              + packageName
              + "不存在，请确认包中PKG-INFO指定"
              + packageName
              + "和实际文件夹名称一致)");
    }
    return createZipFile(file.getInputStream(), packageName, rootPath);
  }

  public static Boolean checkModuleIsExistEnv(String module) {
    // 获取整个pip list
    try {
      if (CollectionUtils.isEmpty(moduleSet)) {
        String piplist = Utils.exec((new String[] {"pip3", "list", "--format=legacy"}));
        String[] split = piplist.split("\\n");
        moduleSet.addAll(Arrays.asList(split));
      }
      if (moduleSet.stream().anyMatch(s -> s.startsWith(module))) {
        return true;
      }
    } catch (Exception e) {
      logger.info("get pip3 list error", e);
    }
    try {
      String exec =
          Utils.exec(
              (new String[] {
                "python3", Configuration.getLinkisHome() + "/admin/" + "check_modules.py", module
              }));
      return Boolean.parseBoolean(exec);
    } catch (Exception e) {
      logger.info("get python3 env check error", e);
      return false;
    }
  }

  public static List<String> getInstallRequestPythonModules(MultipartFile file) throws IOException {
    List<String> modules = new ArrayList<>();
    String originalFilename = file.getOriginalFilename();
    if (StringUtils.isNotBlank(originalFilename)
        && originalFilename.endsWith(Constants.FILE_EXTENSION_TAR_GZ)) {
      int findSetup = 0;
      // 读取 setup.py 文件的内容，并使用正则表达式提取 install_requires 字段。
      // 解析 install_requires 字段中的依赖包信息
      try (TarArchiveInputStream tarInput =
          new TarArchiveInputStream(new GzipCompressorInputStream(file.getInputStream()))) {
        TarArchiveEntry entry;
        while ((entry = tarInput.getNextTarEntry()) != null) {
          if (entry.getName().endsWith("setup.py") || entry.getName().endsWith("pyproject.toml")) {
            findSetup = 1;
            String content = IOUtils.toString(tarInput, StandardCharsets.UTF_8);
            modules = extractDependencies(content);
            break;
          }
        }
      } catch (Exception e) {
        throw new UdfException(80039, "File upload failed, error message:", e);
      }
      if (findSetup == 0) {
        throw new UdfException(
            80040,
            "setup.py or pyproject.toml file not found in the archive (setup.py or pyproject.toml 文件不存在，请确认包中包含文件)");
      }
    }
    return modules;
  }

  public static List<String> extractDependencies(String content) {
    String trim =
        content
            .replaceAll("#.*?\\n", "")
            .replaceAll("\\n", "")
            .replaceAll("'", "")
            .replaceAll(" ", "")
            .trim();
    List<String> modules = new ArrayList<>();
    String moduleStr = "";
    Matcher setupMatcher =
        Pattern.compile("install_requires=\\[(.*?)\\]", Pattern.DOTALL).matcher(trim);
    if (setupMatcher.find()) {
      moduleStr = setupMatcher.group(1);
    }
    Matcher pyprojectMatcher =
        Pattern.compile("dependencies=\\[(.*?)\\]", Pattern.DOTALL).matcher(trim);
    if (pyprojectMatcher.find()) {
      moduleStr = pyprojectMatcher.group(1);
    }
    String[] packages = moduleStr.split(",");
    for (String pkg : packages) {
      pkg =
          pkg.replaceAll("#.*?\\n", "")
              .replaceAll("\\n", "")
              .replaceAll("'", "")
              .replace("\"", "")
              .trim();
      for (String operator : Constants.OPERATORS) {
        if (pkg.contains(operator)) {
          String[] parts = pkg.split(operator);
          pkg = parts[0].trim();
        }
      }
      if (StringUtils.isNotBlank(pkg)) {
        modules.add(pkg);
      }
    }
    return modules;
  }
}
