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

import org.apache.linkis.manager.label.entity.annon.KeyMethod;
import org.apache.linkis.protocol.message.RequestProtocol;

import java.io.Serializable;

public interface Label<T> extends Serializable, RequestProtocol {
    /**
     * Label value relations
     */
    enum ValueRelation {
        /**
         * Relation
         */
        OR, AND, ALL
    }

    /**
     * Label key
     * @return string
     */
    @KeyMethod
    String getLabelKey();
    /**
     * Label value
     * @return T
     */
    T getValue();

    /**
     * Value.asString()
     * @return string
     */
    String getStringValue();

    /**
     * Label feature
     *
     * @return feature enum
     */
    default Feature getFeature(){
        return Feature.OPTIONAL;
    }

    /**
     * Label value isEmpty
     *
     * @return
     */
    Boolean isEmpty();
}
