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

package org.apache.linkis.manager.label.builder;

import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import java.io.InputStream;
import java.lang.reflect.Type;

/** Extend label builder interface */
public interface ExtensibleLabelBuilder {
  /**
   * If accept the key and type of label
   *
   * @param labelKey label key
   * @param labelClass class of label
   * @return boolean
   */
  boolean canBuild(String labelKey, Class<?> labelClass);

  /**
   * Build label by value object(not stream)
   *
   * @param labelKey label key
   * @param valueObj value object
   * @param labelClass class of label </em>interface or entity class</em>
   * @param valueTypes value types
   * @param <T> extends Label
   * @return label
   */
  <T extends Label<?>> T build(
      String labelKey, Object valueObj, Class<?> labelClass, Type... valueTypes)
      throws LabelErrorException;

  <T extends Label<?>> T build(String labelKey, Object valueObj, Class<T> labelType)
      throws LabelErrorException;

  /**
   * Build label by value stream
   *
   * @param labelKey label key
   * @param valueInput value input
   * @param labelClass type of label <em>interface or entity class</em>
   * @param valueTypes value types
   * @param <T> extends Label
   * @return label
   */
  <T extends Label<?>> T build(
      String labelKey, InputStream valueInput, Class<?> labelClass, Type... valueTypes)
      throws LabelErrorException;

  <T extends Label<?>> T build(String labelKey, InputStream valueInput, Class<T> labelClass)
      throws LabelErrorException;

  int getOrder();
}
