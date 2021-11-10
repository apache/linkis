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
 
package org.apache.linkis.manager.label.entity;

import org.apache.linkis.manager.label.constant.LabelKeyConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CombinedLabelImpl implements CombinedLabel {

    private List<Label<?>> value;

    private Feature feature;

    public CombinedLabelImpl() {

    }

    public CombinedLabelImpl(List<Label<?>> value) {
        value.sort(Comparator.comparing(Label::getLabelKey));
        Collections.reverse(value);
        this.value = value;
    }

    @Override
    public String getLabelKey() {
        if (isEmpty()) {
            return LabelKeyConstant.COMBINED_LABEL_KEY_PREFIX;
        }
        List<String> keyList = getValue().stream().map(Label::getLabelKey).collect(Collectors.toList());

        String labelKey = LabelKeyConstant.COMBINED_LABEL_KEY_PREFIX + StringUtils.join(keyList, "_");
        return labelKey;
    }

    @Override
    public List<Label<?>> getValue() {
        return this.value;
    }

    @Override
    public String getStringValue() {
        if (isEmpty()) {
            return null;
        }
        return getValue().stream().map(Label::getStringValue).collect(Collectors.joining(","));
    }

    @Override
    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    @Override
    public Boolean isEmpty() {
        return CollectionUtils.isEmpty(getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj != null && obj instanceof CombinedLabel){
            return getStringValue().equals(((Label) obj).getStringValue());
        }else{
            return false;
        }
    }


    @Override
    public String toString() {
        return "CombinedLabelImpl{" +
                "key=" + getLabelKey() +
                "value=" + getStringValue() +
                '}';
    }
}
