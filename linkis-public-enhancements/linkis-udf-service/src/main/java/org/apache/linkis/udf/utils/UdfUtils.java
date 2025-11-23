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
import org.apache.linkis.common.utils.JsonUtils;
import org.apache.linkis.common.utils.Utils;
import org.apache.linkis.storage.fs.FileSystem;
import org.apache.linkis.storage.utils.StorageUtils$;
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

import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdfUtils {
  private static final Logger logger = LoggerFactory.getLogger(UdfUtils.class);
  private static Set<String> moduleSet = new HashSet<>();

  private static final String setuppyFileName = "setup.py";
  private static final String pyprojecttomlFileName = "pyproject.toml";

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
    String rootPathStr = "";
    try (TarArchiveInputStream tarInput =
        new TarArchiveInputStream(new GzipCompressorInputStream(inputStream))) {
      TarArchiveEntry entry;
      String delimiter = FsPath.SEPARATOR + folder + FsPath.SEPARATOR;
      while ((entry = tarInput.getNextTarEntry()) != null) {
        if (entry.isDirectory() && entry.getName().endsWith(delimiter)) {
          rootPathStr = entry.getName().replace(folder + FsPath.SEPARATOR, "");
          return rootPathStr;
        }
        if (entry.getName().contains(delimiter)) {
          rootPathStr = entry.getName().substring(0, entry.getName().indexOf(delimiter));
          return rootPathStr;
        }
      }
    } catch (Exception e) {
      throw new UdfException(80039, "File upload failed, error message:", e);
    }
    return rootPathStr;
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
    if (StringUtils.isNotBlank(packageName) && StringUtils.isNotBlank(rootPath)) {
      return createZipFile(file.getInputStream(), packageName, rootPath);
    } else {
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
      if (module == null || !module.matches("^[a-zA-Z][a-zA-Z0-9_.-]{0,49}$")) {
        throw new IllegalArgumentException("Invalid module name: " + module);
      }
      String exec =
          Utils.exec(
              (new String[] {
                Constants.PYTHON_COMMAND.getValue(),
                Configuration.getLinkisHome() + "/admin/" + "check_modules.py",
                Constants.PYTHON_PATH.getValue(),
                module
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
          if (entry.getName().endsWith(setuppyFileName)
              || entry.getName().endsWith(pyprojecttomlFileName)) {
            findSetup = 1;
            String content = IOUtils.toString(tarInput, StandardCharsets.UTF_8);
            modules = extractDependencies(content, entry.getName());
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

  public static List<String> extractDependencies(String content, String name) {
    String trim =
        content
            .replaceAll("#.*?\\n", "")
            .replaceAll("\\n", "")
            .replaceAll("'", "")
            .replaceAll(" ", "")
            .trim();
    List<String> modules = new ArrayList<>();
    String moduleStr = "";
    if (name.endsWith(setuppyFileName)) {
      Matcher setupMatcher =
          Pattern.compile("install_requires=\\[(.*?)\\]", Pattern.DOTALL).matcher(trim);
      if (setupMatcher.find()) {
        moduleStr = setupMatcher.group(1);
      }
    }
    if (name.endsWith(pyprojecttomlFileName)) {
      Matcher pyprojectMatcher =
          Pattern.compile("dependencies=\\[(.*?)\\]", Pattern.DOTALL).matcher(trim);
      if (pyprojectMatcher.find()) {
        moduleStr = pyprojectMatcher.group(1);
      }
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
        modules.add(pkg.toLowerCase());
      }
    }
    return modules;
  }

  public static List<String> getRegisterFunctions(FileSystem fileSystem, FsPath fsPath, String path)
      throws Exception {
    if (StringUtils.endsWith(path, Constants.FILE_EXTENSION_PY)) {
      // 解析python文件
      return extractPythonMethodNames(path);
    } else if (StringUtils.endsWith(path, Constants.FILE_EXTENSION_SCALA)) {
      try (InputStream is = fileSystem.read(fsPath)) {
        // 将inputstream内容转换为字符串
        String content = IOUtils.toString(is, StandardCharsets.UTF_8);
        // 解析scala代码
        return extractScalaMethodNames(content);
      } catch (IOException e) {
        throw new UdfException(80042, "Failed to read file: " + path, e);
      }
    } else {
      throw new UdfException(80041, "Unsupported file type: " + path);
    }
  }

  public static List<String> extractScalaMethodNames(String scalaCode) {
    List<String> methodNames = new ArrayList<>();
    // 正则表达式匹配方法定义，包括修饰符
    String regex = "(\\b(private|protected)\\b\\s+)?\\bdef\\s+(\\w+)\\b";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(scalaCode);
    logger.info("use regex to get scala method names.. reg:{}", regex);
    while (matcher.find()) {
      String methodName = matcher.group(3);
      methodNames.add(methodName);
    }

    return methodNames;
  }

  public static List<String> extractPythonMethodNames(String udfPath) throws Exception {
    String localPath = udfPath.replace(StorageUtils$.MODULE$.FILE_SCHEMA(), "");
    String exec =
        Utils.exec(
            (new String[] {
              "sudo",
              Constants.PYTHON_PATH.getValue(),
              Configuration.getLinkisHome() + "/admin/" + "linkis_udf_get_python_methods.py",
              localPath
            }));
    logger.info(
        "execute python script to get python method name...{} {} {} {}",
        "sudo",
        Constants.PYTHON_COMMAND.getValue(),
        Configuration.getLinkisHome() + "/admin/" + "linkis_udf_get_python_methods.py",
        localPath);
    // 将exec转换为List<String>，exec为一个json数组
    return JsonUtils.jackson().readValue(exec, new TypeReference<List<String>>() {});
  }
}
