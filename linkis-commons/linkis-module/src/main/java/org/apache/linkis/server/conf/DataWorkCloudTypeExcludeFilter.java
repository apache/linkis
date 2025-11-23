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

package org.apache.linkis.server.conf;

import org.apache.commons.lang3.StringUtils;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
public class DataWorkCloudTypeExcludeFilter extends TypeExcludeFilter {

  private String[] excludePackages =
      StringUtils.split(ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES().getValue(), ",");

  private String[] excludeClasses =
      StringUtils.split(ServerConfiguration.BDP_SERVER_EXCLUDE_CLASSES().getValue(), ",");

  private String[] excludeAnnotation =
      StringUtils.split(ServerConfiguration.BDP_SERVER_EXCLUDE_ANNOTATION().getValue(), ",");

  @Override
  public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
      throws IOException {
    String className = metadataReader.getClassMetadata().getClassName();

    return Arrays.stream(excludePackages).anyMatch(className::startsWith)
        || Arrays.stream(excludeClasses).anyMatch(className::equals)
        || Arrays.stream(excludeAnnotation)
            .anyMatch(
                annotation -> metadataReader.getAnnotationMetadata().hasAnnotation(annotation));
  }

  @Override
  public boolean equals(Object obj) {
    return (obj != null) && (getClass() == obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
