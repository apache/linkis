/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.linkis.server.conf

import org.apache.commons.lang.StringUtils
import org.springframework.core.`type`.classreading.{MetadataReader, MetadataReaderFactory}
import org.springframework.core.`type`.filter.TypeFilter

class DataWorkCloudCustomExcludeFilter extends TypeFilter {
  private val excludePackages = ServerConfiguration.BDP_SERVER_EXCLUDE_PACKAGES.getValue.split(",").filter(StringUtils.isNotBlank)
  private val excludeClasses = ServerConfiguration.BDP_SERVER_EXCLUDE_CLASSES.getValue.split(",").filter(StringUtils.isNotBlank)
  private val excludeAnnotation = ServerConfiguration.BDP_SERVER_EXCLUDE_ANNOTATION.getValue.split(",").filter(StringUtils.isNotBlank)

  override def `match`(metadataReader: MetadataReader, metadataReaderFactory: MetadataReaderFactory): Boolean = {
    val className = metadataReader.getClassMetadata.getClassName
    excludeClasses.contains(className) ||
     excludePackages.exists(className.startsWith) ||
      excludeAnnotation.exists(metadataReader.getAnnotationMetadata.hasAnnotation)
  }
}
