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

package org.apache.linkis.manager.am.label;

import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.entity.em.EMInstanceLabel;
import org.apache.linkis.manager.label.entity.engine.EngineTypeLabel;
import org.apache.linkis.manager.label.entity.engine.UserCreatorLabel;
import org.apache.linkis.manager.service.common.label.LabelChecker;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
class AMLabelChecker implements LabelChecker {

  @Override
  public boolean checkEngineLabel(List<Label<?>> labelList) {
    return checkCorrespondingLabel(labelList, EngineTypeLabel.class, UserCreatorLabel.class);
  }

  @Override
  public boolean checkEMLabel(List<Label<?>> labelList) {
    return checkCorrespondingLabel(labelList, EMInstanceLabel.class);
  }

  @Override
  public boolean checkCorrespondingLabel(List<Label<?>> labelList, Class... clazz) {
    // TODO: 是否需要做子类的判断
    List<Class<?>> classes = Arrays.asList(clazz);
    return labelList.stream()
        .filter(Objects::nonNull)
        .map(Label::getClass)
        .collect(Collectors.toList())
        .containsAll(classes);
  }
}
