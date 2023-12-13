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

package org.apache.linkis.engineplugin.repl.executor.javarepl;

import org.apache.linkis.engineplugin.repl.errorcode.ReplErrorCodeSummary;
import org.apache.linkis.engineplugin.repl.exception.ReplException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspired by:
 * https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/common/compiler/support/JavassistCompiler.java
 */
public class JavaReplCompiler {

  private static final Logger logger = LoggerFactory.getLogger(JavaReplCompiler.class);

  private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w\\.\\*]+);\n");

  private static final Pattern EXTENDS_PATTERN =
      Pattern.compile("\\s+extends\\s+([\\w\\.]+)[^\\{]*\\{\n");

  private static final Pattern IMPLEMENTS_PATTERN =
      Pattern.compile("\\s+implements\\s+([\\w\\.]+)\\s*\\{\n");

  private static final Pattern METHODS_PATTERN =
      Pattern.compile("(?<=\\})\\s+(private|public|protected)\\s+");

  private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

  private static final Pattern PACKAGE_PATTERN =
      Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9\\.]*);");

  private static final Pattern CLASS_PATTERN =
      Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

  private static final String javaReplClassName = "LinkisJavaRepl";

  public void compileAndExecutor(
      String code, ClassLoader classLoader, String classpathDir, String methodName)
      throws Exception {
    code = code.trim();
    Matcher matcher = PACKAGE_PATTERN.matcher(code);
    String pkg;
    if (matcher.find()) {
      pkg = matcher.group(1);
    } else {
      pkg = "";
    }
    matcher = CLASS_PATTERN.matcher(code);
    Boolean containClass = true;
    String cls;
    if (matcher.find()) {
      cls = matcher.group(1);
    } else {
      cls = javaReplClassName;
      containClass = false;
    }
    String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;

    // Check whether className exists. If so, change the name
    try {
      Class.forName(className, true, classLoader);
      className = className.concat(RandomStringUtils.randomAlphabetic(6));
    } catch (ClassNotFoundException e) {
    }

    logger.info("Java repl start building the class, className: {}", className);

    if (!code.endsWith("}")) {
      throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
    }

    doCompileAndExecutor(classLoader, className, code, containClass, classpathDir, methodName);
  }

  public void doCompileAndExecutor(
      ClassLoader classLoader,
      String name,
      String source,
      Boolean containClass,
      String classpathDir,
      String methodName)
      throws Exception {
    JavaReplBuilder builder = new JavaReplBuilder();
    builder.setClassName(name);

    // process imported classes
    Matcher matcher = IMPORT_PATTERN.matcher(source);
    while (matcher.find()) {
      builder.addImports(matcher.group(1).trim());
      String importCode = matcher.group();
      if (StringUtils.isNotBlank(importCode)) {
        source = source.replaceFirst(importCode, "");
      }
    }

    // process extended super class
    matcher = EXTENDS_PATTERN.matcher(source);
    if (matcher.find()) {
      builder.setSuperClassName(matcher.group(1).trim());
    }

    // process implemented interfaces
    matcher = IMPLEMENTS_PATTERN.matcher(source);
    if (matcher.find()) {
      String[] ifaces = matcher.group(1).trim().split("\\,");
      Arrays.stream(ifaces).forEach(i -> builder.addInterface(i.trim()));
    }

    // process constructors, fields, methods
    String body = "";
    if (containClass) {
      body = source.substring(source.indexOf('{') + 1, source.length() - 1);
    } else {
      body = source;
    }
    String[] methods = METHODS_PATTERN.split(body);
    String className = getSimpleClassName(name);
    Arrays.stream(methods)
        .map(String::trim)
        .filter(m -> !m.isEmpty())
        .forEach(
            method -> {
              if (method.startsWith(className)) {
                builder.addConstructor("public " + method);
              } else if (FIELD_PATTERN.matcher(method).matches()) {
                builder.addField("private " + method);
              } else {
                builder.addMethod("public " + method);
              }
            });
    // compile
    CtClass cls = builder.build(classLoader);
    logger.info("Java repl CtClass build completed, CtClass: {}", cls);

    ClassPool cp = cls.getClassPool();
    if (classLoader == null) {
      classLoader = cp.getClassLoader();
    }
    cp.insertClassPath(new LoaderClassPath(classLoader));
    if (StringUtils.isNotBlank(classpathDir) && FileUtils.isDirectory(new File(classpathDir))) {
      cp.insertClassPath(classpathDir);
    }

    // If methodName is empty, get the first method
    if (StringUtils.isBlank(methodName)) {
      CtMethod[] declaredMethods = cls.getDeclaredMethods();
      String declareMethodName = declaredMethods[0].getName();
      if (ArrayUtils.isEmpty(declaredMethods) || StringUtils.isBlank(declareMethodName)) {
        throw new ReplException(
            ReplErrorCodeSummary.UNABLE_RESOLVE_JAVA_METHOD_NAME.getErrorCode(),
            ReplErrorCodeSummary.UNABLE_RESOLVE_JAVA_METHOD_NAME.getErrorDesc());
      }
      methodName = declareMethodName;
    }

    logger.info("Java repl methodName: {}", methodName);

    logger.info("Java repl {} start executor", className);

    Class<?> clazz = cp.toClass(cls);
    Object obj = clazz.newInstance();
    obj.getClass().getMethod(methodName).invoke(obj);
    logger.info("Java repl {} executor success", className);
  }

  /** get simple class name from qualified class name */
  public static String getSimpleClassName(String qualifiedName) {
    if (null == qualifiedName) {
      return null;
    }
    int i = qualifiedName.lastIndexOf('.');
    return i < 0 ? qualifiedName : qualifiedName.substring(i + 1);
  }
}
