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
 
package org.apache.linkis.manager.label.builder;


import org.apache.linkis.manager.label.entity.Label;
import org.apache.linkis.manager.label.exception.LabelErrorException;

import javax.annotation.Nullable;

/**
 * Store basic method, can be implemented by any user
 */
public interface LabelBuilder {
    /**
     * If accept label key
     * @param labelKey label key
     */
    boolean canBuild(String labelKey);

    /**
     * Build method
     *
     * @param labelKey label key
     * @param valueObj value object
     * @return label
     */
    Label<?> build(String labelKey, @Nullable Object valueObj) throws LabelErrorException;


    default int getOrder(){
        return Integer.MAX_VALUE;
    }
}
