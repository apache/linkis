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

package org.apache.linkis.metadata.query.service.conf;

import org.apache.linkis.common.conf.CommonVars;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.viewfs.Constants;
import org.apache.hadoop.fs.viewfs.ViewFileSystem;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utils to deal with configuration */
public class ConfigurationUtils {

  private static final CommonVars<String> CONFIG_VIEWFS_LINK_FALLBACK =
      CommonVars.apply("wds.linkis.server.mdm.hadoop.conf.link.fallback", "linkFallback");

  private static final CommonVars<String> CONFIG_VIEWFS_LINK_NFLY =
      CommonVars.apply("wds.linkis.server.mdm.hadoop.conf.link.nfly", "linkNfly");
  /** Placeholder */
  private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("<[^>]*?>");

  private static final Function<String, Boolean> FILTER_ALL_CONFIG = config -> false;

  /**
   * Filter configuration
   *
   * @param fileSystem file system
   * @param filterRules filter rules
   * @param uri uri
   * @return props
   */
  public static Map<String, String> filterConfiguration(
      FileSystem fileSystem, List<String> filterRules, URI uri) {
    Map<String, String> filteredProps = new HashMap<>();
    Configuration hadoopConf = fileSystem.getConf();
    List<String> rules =
        Objects.isNull(filterRules) ? new ArrayList<>() : new ArrayList<>(filterRules);
    Function<String, Boolean> acceptableFunction = FILTER_ALL_CONFIG;
    if (fileSystem instanceof ViewFileSystem) {
      acceptableFunction = addViewFileSystemFilterRules(fileSystem, rules, uri);
    }
    Pattern pattern = rulesToPattern(rules);
    Function<String, Boolean> finalAcceptableFunction = acceptableFunction;
    hadoopConf.forEach(
        entry -> {
          String key = entry.getKey();
          if (pattern.matcher(key).matches() || finalAcceptableFunction.apply(key)) {
            filteredProps.put(key, entry.getValue());
          }
        });
    return filteredProps;
  }

  /**
   * Filter rules to pattern
   *
   * @param filterRules filter rules
   * @return pattern
   */
  private static Pattern rulesToPattern(List<String> filterRules) {
    StringBuffer sb = new StringBuffer("^(");
    for (int i = 0; i < filterRules.size(); i++) {
      String rule = filterRules.get(i);
      if (StringUtils.isNotBlank(rule)) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(rule);
        while (matcher.find()) {
          matcher.appendReplacement(sb, "[\\\\s\\\\S]*?");
        }
        matcher.appendTail(sb);
        if (i < filterRules.size() - 1) {
          sb.append("|");
        }
      }
    }
    ;
    sb.append(")$");
    return Pattern.compile(sb.toString().replace(".", "[.]"));
  }

  /**
   * Add filter rules for view FileSystem
   *
   * @param filerRules filter rules
   * @param uri uri
   * @return filter function
   */
  private static Function<String, Boolean> addViewFileSystemFilterRules(
      FileSystem fileSystem, List<String> filerRules, URI uri) {
    String mountTableName =
        Optional.ofNullable(uri)
            .orElse(FileSystem.getDefaultUri(fileSystem.getConf()))
            .getAuthority();
    if (StringUtils.isBlank(mountTableName)) {
      mountTableName = Constants.CONFIG_VIEWFS_DEFAULT_MOUNT_TABLE;
    }
    if (Objects.nonNull(uri) && fileSystem.getScheme().equals(uri.getScheme())) {
      // Just load the default mountable configuration
      String linkPrefix = Constants.CONFIG_VIEWFS_PREFIX + "." + mountTableName + ".";
      String linkBasicPrefix = linkPrefix + Constants.CONFIG_VIEWFS_LINK;
      String linkMergePrefix = linkPrefix + Constants.CONFIG_VIEWFS_LINK_MERGE;
      // linkFallback, linkNfly in HADOOP-13055
      filerRules.add(linkPrefix + Constants.CONFIG_VIEWFS_LINK_MERGE_SLASH);
      filerRules.add(linkPrefix + CONFIG_VIEWFS_LINK_FALLBACK.getValue());
      filerRules.add(linkPrefix + CONFIG_VIEWFS_LINK_NFLY.getValue());
      filerRules.add(linkPrefix + Constants.CONFIG_VIEWFS_HOMEDIR);
      String path = uri.getPath();
      return config ->
          (config.startsWith(linkBasicPrefix)
                  && path.startsWith(config.substring(linkBasicPrefix.length() + 1)))
              || (config.startsWith(linkMergePrefix)
                  && path.startsWith(config.substring(linkMergePrefix.length() + 1)));
    } else {
      // Load in all the mountable configuration
      filerRules.add(Constants.CONFIG_VIEWFS_PREFIX + "." + mountTableName + ".<suffix>");
    }
    return FILTER_ALL_CONFIG;
  }
}
